import secrets
import string
import random
from firebase_admin import firestore
from firebase_functions import https_fn

def generate_fake_auth_uid(length = 28):
    alphabet = string.ascii_letters + string.digits
    return ''.join(secrets.choice(alphabet) for _ in range(length))

@https_fn.on_request()
def populate_leaderboards(req: https_fn.Request) -> https_fn.Response:
    db = firestore.client()
    collection_ref = db.collection("leaderboards") 
    batch = db.batch()
    
    for _ in range(50):
        uid = generate_fake_auth_uid()
        doc_ref = collection_ref.document(uid)
        
        random_water_goals = random.randint(5, 10)
        random_calories_goals = random.randint(5, 10)
        random_exercise_goals = random.randint(5, 10)
        random_steps_goals = random.randint(5, 10)
    
        data = {
            "profilePictureString": "anonymous",
            "username": "Anonymous",
            "healthiestUserScore": random_water_goals + random_calories_goals + random_exercise_goals + random_steps_goals,
            "waterGoalsCompleted": random_water_goals,
            "caloriesGoalsCompleted": random_calories_goals,
            "exerciseGoalsCompleted": random_exercise_goals,
            "stepsGoalsCompleted": random_steps_goals,
            "totalSteps": random_steps_goals * 10000
        }
        
        batch.set(doc_ref, data)
        
    batch.commit()
    
    return https_fn.Response("Successfully added 50 fake users to the leaderboard!")
