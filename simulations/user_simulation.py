import json
import random
import time
import requests
import argparse
import threading

from constants import LOG, RECOMMENDATION_API_URL, RESERVATION_API_URL
from snapping_service import snap_to_position
from user_creation import create_or_get_users

# Areas and Coordinates (Approximate)
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

FILE = "users.json"

JSON_HEADER = {"Content-Type": "application/json"}


def log(payload):
    with open(FILE, "a") as f:
        f.write(f"{json.dumps(payload)},")


def uprint(msg, username):
    print(f"[{username}] {msg}")


def get_recommendation(username: str, origin, destination):
    body = {
        "username": username,
        "origin": origin,
        "destination": destination,
        "options": {},
    }
    try:
        start = time.perf_counter()
        response = requests.post(
            f"{RECOMMENDATION_API_URL}?format=json",
            data=json.dumps(body),
            headers=JSON_HEADER,
        )
        print(
            "Request completed in {0:.0f} seconds".format(time.perf_counter() - start)
        )
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Error retrieving vehicles: {e}")
        return []


def reserve_vehicles(username: str, vehicle_ids: list[str]):
    body = {"username": username, "vehicleIds": vehicle_ids}
    try:
        response = requests.post(
            f"{RESERVATION_API_URL}?format=json",
            data=json.dumps(body),
            headers=JSON_HEADER,
        )
        response.raise_for_status()
        return response.status_code == 200
    except requests.exceptions.RequestException as e:
        print(f"Error reserving vehicles: {e}")
        return None


def cancel_reservation(username: str, vehicle: str):
    try:
        response = requests.post(
            f"{RESERVATION_API_URL}/{username}/cancel/{vehicle}",
        )
        response.raise_for_status()
        return response.status_code == 200
    except requests.exceptions.RequestException as e:
        print(f"Error canceling reservation vehicles: {e}")
        return None


def exit_ride(username: str, vehicle_id: str, location: dict):
    body = {
        "username": username,
        "vehicleId": vehicle_id,
        "location": location,
    }
    try:
        response = requests.post(
            f"{RECOMMENDATION_API_URL}/end",
            data=json.dumps(body),
            headers=JSON_HEADER,
        )
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Error exiting ride: {e}")
        return None


def start_ride(username: str, vehicle_id: str, location: dict):
    body = {
        "username": username,
        "vehicleId": vehicle_id,
        "location": location,
    }
    try:
        response = requests.post(
            f"{RECOMMENDATION_API_URL}/start",
            data=json.dumps(body),
            headers=JSON_HEADER,
        )
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Error exiting ride: {e}")
        return None


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
    destination_area = AREAS[scenario][origin_area_name]
    destination_latitude = random.uniform(
        destination_area["latitude_range"][0], destination_area["latitude_range"][1]
    )
    destination_longitude = random.uniform(
        destination_area["longitude_range"][0], destination_area["longitude_range"][1]
    )

    origin = snap_to_position(origin_latitude, origin_longitude, "foot-walking")
    destination = snap_to_position(
        destination_latitude, destination_longitude, "foot-walking"
    )

    origin = {"latitude": origin[1], "longitude": origin[0]}
    destination = {"latitude": destination[1], "longitude": destination[0]}

    data = {
        "time": when,
        "user": user["username"],
        "origin": origin,
        "destination": destination,
    }
    if LOG:
        log(data)
    uprint(f"Requesting ride from {origin} to {destination}", user["username"])
    paths = get_recommendation(user["username"], origin, destination)
    if not paths or paths == []:
        uprint(
            f"No vehicles available from {origin} to {destination}",
            user["username"],
        )
        return
    uprint(f"Found paths: {paths['paths'][0]}", user["username"])
    vehicles = [path["id"] for path in paths["paths"][0]]
    if vehicles == []:
        uprint("No route available", user["username"])
        return
    uprint(f"Reserving vehicles: {vehicles}", user["username"])
    reserve_vehicles(user["username"], vehicles)
    time.sleep(5)
    for vehicle in vehicles:
        uprint(f"Canceling reservation of {vehicle}", user["username"])
        cancel_reservation(user["username"], vehicle)


def run_simulation(scenario="full"):
    """Runs the vehicle usage simulation."""

    # 1. Create or Get Users
    available_users = create_or_get_users()
    threads = []

    # 3. Run the simulation loop
    start_time = time.time()
    while time.time() - start_time < SIMULATION_DURATION:
        if random.random() < USAGE_PROBABILITY:
            if len(available_users) == 0:
                print("----------------------")
                print("No more available users to simulate.")
                print("----------------------")
                break
            user = random.choice(available_users)
            thread = threading.Thread(
                target=simulate_ride, args=(user, time.time(), scenario)
            )
            available_users.remove(user)
            thread.start()
            threads.append(thread)
        time.sleep(1)  # Simulate time passing

    # Wait for all threads to complete
    for thread in threads:
        thread.join()


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
