<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Exceptions;

use App\Support\Http\Responses\ErrorResponse;
use Illuminate\Validation\ValidationException as BaseValidationException;

/**
 * バリデーションExceptionクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Exceptions
 */
class ValidationException extends BaseValidationException
{
    private ErrorResponse $errorResponse;
}
