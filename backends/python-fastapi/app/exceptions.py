class AppError(Exception):
    def __init__(self, status_code: int, message: str):
        self.status_code = status_code
        self.message = message


def bad_request(message: str = "bad_request") -> AppError:
    return AppError(400, message)


def unauthorized(message: str = "unauthenticated") -> AppError:
    return AppError(401, message)


def forbidden(message: str = "forbidden") -> AppError:
    return AppError(403, message)


def not_found(message: str = "not_found") -> AppError:
    return AppError(404, message)


def conflict(message: str = "conflict") -> AppError:
    return AppError(409, message)


def internal(message: str = "internal_server_error") -> AppError:
    return AppError(500, message)
