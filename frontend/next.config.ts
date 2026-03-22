import type { NextConfig } from "next";
import path from "path";

/**
 * リポジトリ直下に別の lockfile があると Turbopack が誤ったルートを採用し、
 * dev で不安定になることがあるため、この frontend ディレクトリを明示する。
 */
const nextConfig: NextConfig = {
  turbopack: {
    root: path.join(__dirname),
  },
};

export default nextConfig;
