const POSTCODE_JP_BASE =
  "https://apis.postcode-jp.com/api/v6/postcodes";

export type PostcodeJpRow = {
  pref: string;
  city: string;
  town: string;
};

/** Postcode JP API v6（pref,city,town を要求） */
export async function fetchPostcodeJp(
  postal7: string,
  apiKey: string,
): Promise<PostcodeJpRow[]> {
  const url = `${POSTCODE_JP_BASE}/${encodeURIComponent(postal7)}?apiKey=${encodeURIComponent(apiKey)}&pref,city,town`;
  const res = await fetch(url);
  if (!res.ok) {
    throw new Error(`PostcodeJP HTTP ${res.status}`);
  }
  const data: unknown = await res.json();
  if (!Array.isArray(data)) return [];
  return data.map((row) => {
    const r = row as { pref?: string; city?: string; town?: string };
    return {
      pref: String(r.pref ?? ""),
      city: String(r.city ?? ""),
      town: String(r.town ?? ""),
    };
  });
}

/** 市区町村欄用: API の city + town を連結（例: 港区 + 北青山） */
export function formatCityWard(row: PostcodeJpRow): string {
  return `${row.city}${row.town}`;
}
