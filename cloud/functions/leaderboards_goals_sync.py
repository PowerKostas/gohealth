from firebase_admin import firestore
from firebase_functions import firestore_fn
from google.cloud.firestore import Increment
from datetime import datetime, timezone

# To match the UI, it rounds 0-4 down to 0, leaves 5 as 5, and rounds 6-9 up to 10
def round_value(value: float) -> int:
    val_int = int(value) 
    remainder = val_int % 10
    
    if remainder < 5:
        return val_int - remainder
    
    elif remainder == 5:
        return val_int
    
    else:
        return val_int + (10 - remainder)
    

@firestore_fn.on_document_written(
    document = "users/{uid}/daily_trackings/{logDate}",
    service_account = "healthterra-functions-sa@gohealth-bbb5c556.iam.gserviceaccount.com"
)
def perform_leaderboards_goals_sync(event: firestore_fn.Event[firestore_fn.Change[firestore_fn.DocumentSnapshot]]) -> None: # Triggers on any change on any daily_trackings table
    # If the document was deleted, return instantly
    if not event.data.after or not event.data.after.exists:
        return
    
    # To avoid cheating, if the date is more than 1 day in the future, reject the sync. It's 1 day to account for timezones. For the past it's 7
    # days to allow offline users
    document_date = datetime.strptime(event.params["logDate"], "%Y-%m-%d").date()
    server_date = datetime.now(timezone.utc).date()
    day_difference = (document_date - server_date).days
        
    if day_difference < -7 or day_difference > 1:
        return

    # Before and after states to prevent double counting
    before_data = event.data.before.to_dict() if event.data.before and event.data.before.exists else {}
    after_data = event.data.after.to_dict() if event.data.after and event.data.after.exists else {}

    # 1 = Fallback value, triggers if daily_trackings was just created / deleted and is null, it's 1 so it's bigger than the 0 fallback value
    # of progress
    water_goal_before = before_data.get("waterGoal", 1)
    calories_goal_before = before_data.get("caloriesGoal", 10) # 10 to fix rounding bug
    exercise_goal_before = before_data.get("exerciseGoal", 1)
    steps_goal_before = before_data.get("stepsGoal", 1)

    water_goal_after = after_data.get("waterGoal", 1)
    calories_goal_after = after_data.get("caloriesGoal", 10)
    exercise_goal_after = after_data.get("exerciseGoal", 1)
    steps_goal_after = after_data.get("stepsGoal", 1)

    # Goal completions, +1 if goal achieved (went to met from not met), -1 if goal failed (went to not met from met, user took back inputs or 
    # increased some goal), +0 if unchanged (status stayed the same [still met, or still unmet])
    water_before = before_data.get("waterProgress", 0)
    water_after = after_data.get("waterProgress", 0)
    water_delta = int(water_after >= water_goal_after) - int(water_before >= water_goal_before)

    min_calories_goal_before = round_value(calories_goal_before * 0.9)
    max_calories_goal_before = round_value(calories_goal_before * 1.1)
    calories_before = before_data.get("caloriesProgress", 0)
    calories_before_in_range = max_calories_goal_before >= calories_before >= min_calories_goal_before

    min_calories_goal_after = round_value(calories_goal_after * 0.9)
    max_calories_goal_after = round_value(calories_goal_after * 1.1)
    calories_after = after_data.get("caloriesProgress", 0)
    calories_after_in_range = max_calories_goal_after >= calories_after >= min_calories_goal_after

    calories_delta = int(calories_after_in_range) - int(calories_before_in_range)

    exercise_before = before_data.get("exerciseProgress", 0)
    exercise_after = after_data.get("exerciseProgress", 0)
    exercise_delta = int(exercise_after >= exercise_goal_after) - int(exercise_before >= exercise_goal_before)

    steps_before = before_data.get("stepsProgress", 0)
    steps_after = after_data.get("stepsProgress", 0)
    steps_delta = steps_after - steps_before
    steps_goal_delta = int(steps_after >= steps_goal_after) - int(steps_before >= steps_goal_before)

    # Calculates steps over the goal
    extra_steps_before = max(0, steps_before - steps_goal_before)
    extra_steps_after = max(0, steps_after - steps_goal_after)
    extra_steps_delta = extra_steps_after - extra_steps_before

    # Calculates this user's score for the healthiest user tab, every goal = 1 point, every extra 10000 steps = 0.5 points (5000 steps = 0.25 points ...)
    healthiest_user_score_delta = water_delta + calories_delta + exercise_delta + steps_goal_delta + extra_steps_delta * (0.5 / 10000)

    # Commits to the leaderboards document
    if any([water_delta, calories_delta, exercise_delta, steps_goal_delta, steps_delta, extra_steps_delta]):
        firestore.client().collection("leaderboards").document(event.params["uid"]).set({
            "waterGoalsCompleted": Increment(water_delta),
            "caloriesGoalsCompleted": Increment(calories_delta),
            "exerciseGoalsCompleted": Increment(exercise_delta),
            "stepsGoalsCompleted": Increment(steps_goal_delta),
            "totalSteps": Increment(steps_delta),
            "healthiestUserScore": Increment(healthiest_user_score_delta)
        }, merge = True)
