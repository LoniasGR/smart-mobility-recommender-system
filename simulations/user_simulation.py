import json
import random
import time
import requests
import argparse

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
    "tubitak": {
        "Tubitak": {
            "latitude_range": (40.75, 40.81),
            "longitude_range": (29.41, 29.47),
        },
    },
    "halic": {
        "Halic": {"latitude_range": (41.01, 41.05), "longitude_range": (28.93, 28.97)},
    },
    "full": {
        "Besiktas": {
            "latitude_range": (41.03, 41.06),
            "longitude_range": (29.00, 29.04),
        },
        "Kadikoy": {
            "latitude_range": (40.98, 41.01),
            "longitude_range": (29.02, 29.06),
        },
        "Karaköy": {
            "latitude_range": (41.00, 40.04),
            "longitude_range": (28.95, 28.97),
        },
    },
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
        start = time.perf_counter()
        response = requests.post(
            f"{RECOMMENDATION_API_URL}/{username}?format=json",
            data=json.dumps(body),
            headers=JSON_HEADER,
        )
        print(
            "Request completed in {0:.0f} seconds".format(time.perf_counter() - start)
        )
        response.raise_for_status()
        return response.json()  # Assuming the API returns a JSON list of vehicles
    except requests.exceptions.RequestException as e:
        print(f"Error retrieving vehicles: {e}")
        return []


def simulate_ride(user, when, scenario="full"):
    """Simulates a user taking a ride on a vehicle."""
    origin_area_name = random.choice(list(AREAS[scenario].keys()))
    origin_area = AREAS[scenario][origin_area_name]
    origin_latitude = random.uniform(
        origin_area["latitude_range"][0], origin_area["latitude_range"][1]
    )
    origin_longitude = random.uniform(
        origin_area["longitude_range"][0], origin_area["longitude_range"][1]
    )

    destination_area_name = random.choice(list(AREAS[scenario].keys()))
    destination_area = AREAS[scenario][destination_area_name]
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


def run_simulation(scenario="full"):
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
                simulate_ride(user, time.time(), scenario)
        time.sleep(1)  # Simulate time passing


def parse_args():
    parser = argparse.ArgumentParser(description="Multiply a number by 2.")
    parser.add_argument(
        "scenario",
        choices=["tubitak", "halic", "full"],
        default="full",
        help="the scenario to create",
    )
    args = parser.parse_args()
    return args.scenario


if __name__ == "__main__":
    scenario = parse_args()
    if LOG:
        with open(FILE, "w") as f:
            f.write("[")
    run_simulation(scenario)
    if LOG:
        with open(FILE, "a") as f:
            f.write("]")
