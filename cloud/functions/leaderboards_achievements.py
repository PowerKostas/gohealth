from firebase_admin import firestore
from firebase_functions import firestore_fn

@firestore_fn.on_document_written(
    document = "leaderboards/{uid}",
    service_account = "healthterra-functions-sa@gohealth-bbb5c556.iam.gserviceaccount.com"
)
def calculate_leaderboards_achievements(event: firestore_fn.Event[firestore_fn.Change[firestore_fn.DocumentSnapshot]]) -> None:
    leaderboards_size = 50

    db = firestore.client()
    uid = event.params["uid"]
    
    before_data = event.data.before.to_dict() if event.data.before and event.data.before.exists else {}
    after_data = event.data.after.to_dict() if event.data.after and event.data.after.exists else {}

    # Maps database fields to their respective achievement columns
    categories = {
        "waterGoalsCompleted": "appearWaterLeaderboards",
        "caloriesGoalsCompleted": "appearCaloriesLeaderboards",
        "exerciseGoalsCompleted": "appearExerciseLeaderboards",
        "stepsGoalsCompleted": "appearStepsLeaderboards",
        "totalSteps": "appearTotalStepsLeaderboards"
    }

    leaderboards_thresholds_doc = db.collection("app_state").document("leaderboards_thresholds").get()
    leaderboards_thresholds = leaderboards_thresholds_doc.to_dict() if leaderboards_thresholds_doc.exists else {}

    achievements_unlocked = {}
    categories_to_recalculate = []
    categories_to_recalculate_top = []

    # Assumes they are first in all leaderboards until proven otherwise
    is_first_in_all = True

    # Evaluates each category
    for category, achievement in categories.items():
        before_category_score = before_data.get(category, 0)
        after_category_score = after_data.get(category, 0)
        
        current_threshold = leaderboards_thresholds.get(category, 1) 
        current_threshold_top = leaderboards_thresholds.get(f"{category}Top", 1)

        # Checks if they are first for the secret achievement
        if after_category_score < current_threshold_top:
            is_first_in_all = False

        # Score didn't change, skip
        if before_category_score == after_category_score:
            continue
        
        # Checks if they appeared on the category leaderboards, the achievement is given even in ties
        if before_category_score < current_threshold <= after_category_score:
            achievements_unlocked[achievement] = True

        # Checks if the threshold needs to be recalculated
        crossed_up = before_category_score < current_threshold <= after_category_score
        crossed_down = before_category_score >= current_threshold > after_category_score
        anchor_moved = before_category_score == current_threshold

        if crossed_up or crossed_down or anchor_moved:
            categories_to_recalculate.append(category)

        # Checks if the top threshold needs to be recalculated, doesn't need crossed down because the only person who can pull the threshold down
        # is top user
        crossed_up_top = before_category_score < current_threshold_top <= after_category_score
        top_moved = before_category_score == current_threshold_top

        if crossed_up_top or top_moved:
            categories_to_recalculate_top.append(category)

    # Checks if the secret achievement was unlocked
    if is_first_in_all:
        achievements_unlocked["secret"] = True

    # Grants achievements
    if achievements_unlocked:
        user_ref = db.collection("users").document(uid)
        user_ref.set({"achievements": achievements_unlocked}, merge = True)

    # To save reads, it recalculates the thresholds fields only when they change
    if categories_to_recalculate or categories_to_recalculate_top:
        new_thresholds = {}
        for category in categories_to_recalculate:
            query = db.collection("leaderboards").order_by(category, direction = firestore.Query.DESCENDING).limit(leaderboards_size).get()
                
            # The threshold is the score of the Nth user
            new_thresholds[category] = query[-1].get(category)

        for category in categories_to_recalculate_top:
            query = db.collection("leaderboards").order_by(category, direction = firestore.Query.DESCENDING).limit(1).get()
      
            # The threshold is the score of the first user
            new_thresholds[f"{category}Top"] = query[0].get(category)
        
        db.collection("app_state").document("leaderboards_thresholds").set(new_thresholds, merge = True)
