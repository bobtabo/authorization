/**
 * サーバーサイドページング ヘルパーモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

const DEFAULT_PAGE_COUNT = 5;

export interface PagerResponse {
  count: number;
  limit: number;
  next: boolean;
  previous: boolean;
  page: number;
  nextPage: number;
  previousPage: number;
  pageCount: number;
  first: boolean;
  last: boolean;
  firstRecordCount: number;
  lastRecordCount: number;
  startPage: number;
  endPage: number;
}

export function buildPager(count: number, limit: number, offset: number, recordCount: number): PagerResponse {
  if (limit <= 0) limit = 10;

  let pageCount = Math.max(1, Math.ceil(count / limit));
  const lastPageOffset = (pageCount * limit) - limit;
  if (count > 0 && offset > lastPageOffset) {
    offset = lastPageOffset;
  }

  const page = Math.floor(Math.ceil(offset / limit)) + 1;

  let startPage = page - (DEFAULT_PAGE_COUNT - 1);
  if (startPage <= 0) startPage = 1;
  let endPage = startPage + (DEFAULT_PAGE_COUNT - 1);
  if (endPage > pageCount) endPage = pageCount;

  return {
    count,
    limit,
    next: pageCount > page,
    previous: page > 1,
    page,
    nextPage: page + 1,
    previousPage: page - 1,
    pageCount,
    first: page > 1,
    last: pageCount > page,
    firstRecordCount: offset + 1,
    lastRecordCount: offset + recordCount,
    startPage,
    endPage,
  };
}
