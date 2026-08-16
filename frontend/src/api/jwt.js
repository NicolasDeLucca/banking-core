/** Decodes a JWT's payload client-side, purely to read id/email/role for the UI.
 *  This is NOT verification - the token is still validated server-side on every
 *  request; the frontend just trusts it enough to decide what to show.
 */
export function decodeJwt(token) {
  try {
    const payload = token.split(".")[1];
    const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(normalized)
        .split("")
        .map((c) => "%" + c.charCodeAt(0).toString(16).padStart(2, "0"))
        .join("")
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}
