import json
import random
import time
import requests

# API Endpoints
ROOT_URL = "http://localhost:8080"
RECOMMENDATION_API_URL = f"{ROOT_URL}/api/recommend"
USER_API_URL = f"{ROOT_URL}/api/users"
RIDE_API_URL = f"{ROOT_URL}/single-ride/"

# Predefined List of Users
USERS = [
    {"username": "ayse123", "dateOfBirth": "1990-05-15", "gender": "FEMALE"},
    {"username": "mehmet456", "dateOfBirth": "1985-12-20", "gender": "MALE"},
    {"username": "fatma789", "dateOfBirth": "1995-08-01", "gender": "FEMALE"},
    {"username": "ali012", "dateOfBirth": "1980-03-10", "gender": "MALE"},
    {"username": "zeynep345", "dateOfBirth": "2000-11-25", "gender": "FEMALE"},
]

# Areas and Coordinates (Approximate - adjust based on your map data)
AREAS = {
    "Besiktas": {"latitude_range": (41.03, 41.06), "longitude_range": (29.00, 29.04)},
    "Kadikoy": {"latitude_range": (40.98, 41.01), "longitude_range": (29.02, 29.06)},
    "Adalar": {"latitude_range": (40.85, 40.92), "longitude_range": (29.07, 29.15)},
}

# Simulation Parameters
SIMULATION_DURATION = 60  # Seconds
USAGE_PROBABILITY = 0.2  # Probability of a vehicle being used in each iteration
MAX_TRIP_DISTANCE = 5  # Maximum trip distance in kilometers


JSON_HEADER = {"Content-type": "application/json"}

FILE = "users.json"
LOG = True


def log(payload):
    with open(FILE, "a") as f:
        f.write(f"{json.dumps(payload)},")


def create_user(user_data):
    """Creates a user using the API."""
    try:
        response = requests.post(
            USER_API_URL, data=json.dumps(user_data), headers=JSON_HEADER
        )
        response.raise_for_status()
        print(f"User {user_data['username']} created successfully.")
    except requests.exceptions.RequestException as e:
        print(f"Error creating user {user_data['username']}: {e}")


def get_recommendation(username: str, origin, destination):
    body = {"origin": origin, "destination": destination, "options": {}}
    try:
        response = requests.get(
            f"{RECOMMENDATION_API_URL}/{username}?format=json",
            data=json.dumps(body),
            headers=JSON_HEADER,
        )  # Replace with your actual API endpoint
        response.raise_for_status()
        return response.json()  # Assuming the API returns a JSON list of vehicles
    except requests.exceptions.RequestException as e:
        print(f"Error retrieving vehicles: {e}")
        return []


def simulate_ride(user, when):
    """Simulates a user taking a ride on a vehicle."""
    origin_area_name = random.choice(list(AREAS.keys()))
    origin_area = AREAS[origin_area_name]
    origin_latitude = random.uniform(
        origin_area["latitude_range"][0], origin_area["latitude_range"][1]
    )
    origin_longitude = random.uniform(
        origin_area["longitude_range"][0], origin_area["longitude_range"][1]
    )

    destination_area_name = random.choice(list(AREAS.keys()))
    destination_area = AREAS[destination_area_name]
    destination_latitude = random.uniform(
        destination_area["latitude_range"][0], destination_area["latitude_range"][1]
    )
    destination_longitude = random.uniform(
        destination_area["longitude_range"][0], destination_area["longitude_range"][1]
    )

    origin = {"latitude": origin_latitude, "longitude": origin_longitude}
    destination = {"latitude": destination_latitude, "longitude": destination_longitude}

    data = {
        "time": when,
        "user": user["username"],
        "origin": origin,
        "destination": destination,
    }
    if LOG:
        log(data)
    paths = get_recommendation(user["username"], origin, destination)
    print(paths)

    # payload = {
    #     "vehicleId": paths[0][0]["id"],
    #     "destination": {
    #         "latitude": destination_latitude,
    #         "longitude": destination_longitude,
    #     },
    # }
    # ride_url = RIDE_API_URL.format(username=user["username"])

    # try:
    #     response = requests.post(
    #         ride_url, data=json.dumps(payload), headers=JSON_HEADER
    #     )
    #     response.raise_for_status()
    #     print(
    #         f"User {user['username']} took vehicle {vehicle['id']} to ({destination_latitude:.4f}, {destination_longitude:.4f})"
    #     )
    # except requests.exceptions.RequestException as e:
    #     print(
    #         f"Error simulating ride for user {user['username']} and vehicle {vehicle['id']}: {e}"
    #     )


def run_simulation():
    """Runs the vehicle usage simulation."""

    # 1. Create Users
    for user_data in USERS:
        create_user(user_data)

    # 3. Run the simulation loop
    start_time = time.time()
    while time.time() - start_time < SIMULATION_DURATION:
        for user in USERS:
            if random.random() < USAGE_PROBABILITY:
                user = random.choice(USERS)
                simulate_ride(user, time.time())
        time.sleep(1)  # Simulate time passing


if __name__ == "__main__":
    if LOG:
        with open(FILE, "w") as f:
            f.write("[")
    run_simulation()
    if LOG:
        with open(FILE, "a") as f:
            f.write("]")
