/**
 * AutoMapper シングルトンモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { createMapper } from "@automapper/core";
import { pojos } from "@automapper/pojos";

export const mapper = createMapper({ strategyInitializer: pojos() });
