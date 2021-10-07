import { Equals, Equatable } from "./Equatable";

export interface Hashable<A> extends Equatable<A> {
  hash(): number;
}

export interface Hash<A> extends Equals<A> {
  hash(a: A): number;
}

export const numberHash: Hash<number> = {
  isEqual(l: number, r: number): boolean {
    return l === r;
  },
  hash(a: number): number {
    return a;
  },
};

export function hashableHash<A extends Hashable<A>>(): Hash<A> {
  return {
    isEqual(l: A, r: A): boolean {
      return l.equals(r);
    },
    hash(a: A): number {
      return a.hash();
    },
  };
}
