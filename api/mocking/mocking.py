import argparse
import requests
import json
import uuid
import random
from faker import Faker

OPTIONS = ["vehicle", "person", "vehicle_status", "vehicle_use"]
HOSTNAME = "http://localhost:8080"

LOCATIONS = [
# (lat, long)
(41.0085, 28.9799),
(41.0115, 28.9833),
(41.0054, 28.9768),
(41.0256, 28.9744),
(41.0105, 28.9688),
(41.0365, 28.9850),
(41.0387, 28.9981),
(41.0225, 29.0059),
(41.0167, 28.9603),
(41.0172, 28.9708),
(41.00501,29.01697),
]


vehicle_template = {"id": "", "type": ""}

vehicle_status_template = {
    "latitude": "",
    "longitude": "",
    "battery": "",
    "status": "",
}

vehicle_use_template = {
    "startingTime": "",
    "startingLatitude": "",
    "startingLongitude": "",
    "endingTime": "",
    "endingLatitude": "",
    "endingLongitude": "",
    "vehicle": "",
}

person_template = {"username": ""}

groups = {
    "vehicle": {
        "url": f"{HOSTNAME}/api/vehicles",
        "template": vehicle_template,
        "method": "post",
    },
    "person": {
        "method": "post",
        "template": person_template,
        "url": f"{HOSTNAME}/api/people",
    },
    "vehicle_status": {
        "method": "put",
        "template": vehicle_status_template,
        "url": f"{HOSTNAME}/api/vehicles/",
    },
    "vehicle_use": {
        "method": "post",
        "template": vehicle_use_template,
        "url": f"{HOSTNAME}/api/people/",
    },
}


def send_request(
    method: str, url: str, data: dict = dict()
) -> requests.Response | None:
    headers = {"Content-Type": "application/json"}
    response = requests.request(method, url, data=json.dumps(data), headers=headers)

    if 200 <= response.status_code < 300:
        print(f"{method.upper()} request successful!")
        return response
    else:
        print(
            f"{method.upper()} request failed with status code: {response.status_code} and payload {response.text}"
        )

def location_pertrubations(location) {
return (
    random.uniform(location[0] - 0.050, location[0] + 0.050),
    random.uniform(location[1] - 0.050, location[1] + 0.050)
    )
}
def create_payload_field(key: str):
    fake = Faker()
    location = random.choice(LOCATIONS)
    match key:
        case "id":
            return str(uuid.uuid4())
        case "username":
            return fake.user_name()
        case "type":
            return random.choice(["SEA_VESSEL", "CAR", "SCOOTER"])
        case "gender":
            return random.choice(["MALE", "FEMALE", "OTHER"])
        case "battery":
            return random.uniform(0, 1)
        case "latitude" | "startingLatitude" | "endingLatitude":
            return location[0]
        case "longitude" | "startingLongitude" | "endingLongitude":
            return location[1]
        case "startingTime" | "endingTime":
            return fake.date_time_this_month().isoformat()
        case "status":
            return random.choice(["IN_USE", "IDLE", "CHARGING"])


def create_payload(template) -> dict:
    payload = dict()
    for k in template.keys():
        payload[k] = create_payload_field(k)
    return payload


def parse_input():
    parser = argparse.ArgumentParser(
        description="A tool to create mock data for the smart-mobility recommender system"
    )
    parser.add_argument("-c", "--count", help="Count of items to create")
    parser.add_argument(
        "-t",
        "--type",
        help="Type of data to generate",
        choices=OPTIONS,
    )

    return parser.parse_args()


def handle_input():
    args = parse_input()
    count = args.count
    type = args.type
    if type == None:
        type: str = input(
            f"What kind of data should I be creating? Options are {OPTIONS}: "
        )
        while type not in OPTIONS:
            type = input(f"Not a valid option. Please pick one of {OPTIONS}: ")
    if count == None:
        count: str = input(f"How many items of type {type} should be created? ")
    return (type, int(count))


if __name__ == "__main__":
    (group_type, count) = handle_input()

    group = groups[group_type]

    url: str = group["url"]
    template = group["template"]
    method: str = group["method"]

    if group_type == "vehicle_status" or group_type == "vehicle_use":
        vehicle_data = send_request("get", groups["vehicle"]["url"]).json()

    if group_type == "vehicle_use":
        user_data = send_request("get", groups["person"]["url"]).json()

    for i in range(count):

        payload = create_payload(template)
        print(payload)
        if group_type == "vehicle_status":
            selected_vehicle = random.choice(vehicle_data)
            url = f"{group['url']}{selected_vehicle['id']}"
            payload["id"] = selected_vehicle["id"]

        if group_type == "vehicle_use":
            selected_user = random.choice(user_data)
            selected_vehicle = random.choice(vehicle_data)
            url = f"{group['url']}{selected_user['username']}/ride"
            payload["vehicle"] = {"id": selected_vehicle["id"]}
            print(payload)

        send_request(method, url, payload)
