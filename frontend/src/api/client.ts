import axios from "axios";

const baseURL = import.meta.env.VITE_API_URL;

if (!baseURL) {
  throw new Error("VITE_API_URL is not set. Please check your .env file.");
}

/**
 * Common HTTP client.
 * Backend language is irrelevant as long as HTTP contract is matched.
 */
export const apiClient = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10000,
});
