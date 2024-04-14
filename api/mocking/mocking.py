import argparse
import requests
import json
import uuid
import random
from faker import Faker

OPTIONS = ["vehicle", "person", "vehicle_stats"]

vehicle_template = {"id": "", "type": ""}

vehicle_status_status = {
    "latitude": "",
    "longitude": "",
    "battery": "",
    "vehicleStatus": "",
}

person_template = {"username": ""}

groups = {
    "vehicle": {
        "url": "http://localhost:8080/api/vehicles",
        "template": vehicle_template,
        "method": "post",
    },
    "person": {
        "method": "post",
        "template": person_template,
        "url": "http://localhost:8080/api/people",
    },
    "vehicle_status": {
        "method": "put",
        "template": vehicle_status_status,
        "url": "http://localhost:8080/api/vehicles/",
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


def create_payload_field(key: str):
    fake = Faker()
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
        case "latitude":
            return random.uniform(0, 90)
        case "longitude":
            return random.uniform(0, 90)
        case "vehicleStatus":
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

    for i in range(count):

        payload = create_payload(template)

        if group_type == "vehicle_status":
            data = send_request("get", groups["vehicle"]["url"]).json()
            selected_vehicle = random.choice(data)
            url = f"{url}{selected_vehicle['id']}"
            payload["id"] = selected_vehicle["id"]

        send_request(method, url, payload)
