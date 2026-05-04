/**
 * @automapper/core と @automapper/pojos の型宣言シム。
 *
 * @automapper/core v8 は "type":"module" だが exports フィールドを持たず、
 * NodeNext moduleResolution では ESM 内部の拡張子なし import が解決できないため、
 * 使用する API のみ ambient declare module で上書きする。
 */

declare module "@automapper/core" {
  export interface Mapper {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    map(sourceObject: object, sourceSymbol: string | symbol, destinationSymbol: string | symbol): any;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    mapArray(sourceObjects: object[], sourceSymbol: string | symbol, destinationSymbol: string | symbol): any;
  }

  export function createMapper(options: { strategyInitializer: unknown }): Mapper;

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  export function createMap<TSource = any, TDestination = any>(
    mapper: Mapper,
    sourceSymbol: string | symbol,
    destinationSymbol: string | symbol,
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    ...configurations: any[]
  ): void;

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  export function forMember(selector: (destination: any) => any, configuration: unknown): unknown;

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  export function mapFrom(expression: (source: any) => any): unknown;
}

declare module "@automapper/pojos" {
  export function pojos(): unknown;

  export class PojosMetadataMap {
    static create<TModel extends object>(
      identifier: string | symbol,
      metadata?: { [key in keyof TModel]?: unknown },
    ): void;
    static reset(): void;
  }
}
