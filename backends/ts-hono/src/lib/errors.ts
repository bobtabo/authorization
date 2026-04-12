export class AppError extends Error {
  constructor(
    public readonly statusCode: number,
    public readonly message: string,
  ) {
    super(message);
  }
}

export const badRequest  = (msg = "bad_request")          => new AppError(400, msg);
export const unauthorized = (msg = "unauthenticated")      => new AppError(401, msg);
export const forbidden   = (msg = "forbidden")             => new AppError(403, msg);
export const notFound    = (msg = "not_found")             => new AppError(404, msg);
export const conflict    = (msg = "conflict")              => new AppError(409, msg);
export const internal    = (msg = "internal_server_error") => new AppError(500, msg);
