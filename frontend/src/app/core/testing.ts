/** Builds an unsigned JWT-shaped string for tests (the client never verifies signatures). */
export function fakeJwt(claims: Record<string, unknown>): string {
  const encode = (value: object) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'HS256' })}.${encode(claims)}.signature`;
}
