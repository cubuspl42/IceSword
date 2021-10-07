export interface Equatable<A> {
  equals(other: A): boolean;
}

export interface Equals<A> {
  isEqual(l: A, r: A): boolean;
}

export const referenceEquals: Equals<any> = {
  isEqual(l: any, r: any): boolean {
    return l === r;
  }
};

export function equatableEquals<A extends Equatable<A>>() {
  return {
    equals(l: A, r: A): boolean {
      return l.equals(r);
    }
  };
}
