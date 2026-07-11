#!/usr/bin/env python3
"""
LocalStack SES → MailPit relay
LocalStack Community は SES SMTP リレーをサポートしていないため、
/_aws/ses API をポーリングして MailPit に転送する
"""
import json
import smtplib
import time
import urllib.request
from email import message_from_string

LOCALSTACK_URL = "http://localstack:4566"
MAILPIT_HOST = "mailpit"
MAILPIT_PORT = 1025
POLL_INTERVAL = 2

seen_ids = set()


def send_to_mailpit(raw: str, source: str, to_addrs: list) -> None:
    with smtplib.SMTP(MAILPIT_HOST, MAILPIT_PORT, timeout=5) as smtp:
        smtp.sendmail(source, to_addrs, raw.encode("utf-8"))


def build_raw_from_json(msg: dict) -> tuple:
    source = msg.get("Source", "")
    to = msg.get("Destination", {}).get("ToAddresses", [])
    subject = msg.get("Subject", "")
    body = msg.get("Body", {})
    html = body.get("html_part") or ""
    text = body.get("text_part") or ""

    content_type = "text/html; charset=utf-8" if html else "text/plain; charset=utf-8"
    content = html or text
    raw = "\r\n".join([
        f"From: {source}",
        f"To: {', '.join(to)}",
        f"Subject: {subject}",
        "MIME-Version: 1.0",
        f"Content-Type: {content_type}",
        "",
        content,
    ])
    return raw, source, to


def relay_pending() -> None:
    req = urllib.request.urlopen(f"{LOCALSTACK_URL}/_aws/ses", timeout=5)
    data = json.loads(req.read())

    for msg in data.get("messages", []):
        msg_id = msg["Id"]
        if msg_id in seen_ids:
            continue
        seen_ids.add(msg_id)

        raw = msg.get("RawData")
        if raw:
            parsed = message_from_string(raw)
            source = parsed["From"] or msg.get("Source", "")
            to_addrs = parsed.get_all("To") or []
        else:
            raw, source, to_addrs = build_raw_from_json(msg)

        send_to_mailpit(raw, source, to_addrs)
        print(f"[relay] forwarded {msg_id} to MailPit", flush=True)


def wait_for_localstack() -> None:
    print("[relay] waiting for LocalStack...", flush=True)
    while True:
        try:
            urllib.request.urlopen(f"{LOCALSTACK_URL}/_localstack/health", timeout=3)
            print("[relay] LocalStack is ready", flush=True)
            return
        except Exception:
            time.sleep(3)


if __name__ == "__main__":
    wait_for_localstack()
    print(f"[relay] polling {LOCALSTACK_URL}/_aws/ses every {POLL_INTERVAL}s", flush=True)
    while True:
        try:
            relay_pending()
        except Exception as e:
            print(f"[relay] error: {e}", flush=True)
        time.sleep(POLL_INTERVAL)
