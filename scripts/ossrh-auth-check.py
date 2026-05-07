#!/usr/bin/env python3
import base64
import os
import sys
import urllib.request
from urllib.error import HTTPError

username = os.getenv("OSSRH_USERNAME", "")
password = os.getenv("OSSRH_PASSWORD", "")

if not username or not password:
    print("Missing OSSRH_USERNAME or OSSRH_PASSWORD")
    sys.exit(1)

url = "https://s01.oss.sonatype.org/service/local/staging/profiles"
request = urllib.request.Request(url)
auth = base64.b64encode(f"{username}:{password}".encode("utf-8")).decode("utf-8")
request.add_header("Authorization", f"Basic {auth}")

try:
    with urllib.request.urlopen(request, timeout=30) as response:
        print(f"HTTP {response.status}")
        print("OSSRH auth OK")
except HTTPError as error:
    if error.code == 402:
        print("HTTP 402 from legacy OSSRH endpoint")
        print("Central Portal deployments can still succeed with the configured credentials.")
        print("Treat this as informational unless you are doing a classic OSSRH snapshot deploy.")
        sys.exit(0)
    print(f"OSSRH auth failed: {error}")
    sys.exit(1)
except Exception as error:
    print(f"OSSRH auth failed: {error}")
    sys.exit(1)