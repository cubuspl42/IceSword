import { Hash } from "./Hashable";

type Bucket<K, V> = Set<[K, V]> | [K, V];

class PeekIterator<E> {
  private peekedEntry: E | undefined = undefined;

  constructor(
    private readonly iterator: IterableIterator<E>,
  ) {
    this._peek();
  }

  next(): E {
    const entry = this.peekedEntry;

    if (entry === undefined) {
      throw new Error("Iterator is done");
    }

    this._peek();

    return entry;
  }

  hasNext(): boolean {
    return this.peekedEntry !== undefined;
  }

  private _peek() {
    const innerResult = this.iterator.next();
    this.peekedEntry = innerResult.value;
  }
}


export class HashMap<K, V> {
  constructor(
    entries: Iterable<[K, V]> | null,
    private readonly _keyHash: Hash<K>,
  ) {
    if (entries !== null) {
      for (const [k, v] of entries) {
        this.set(k, v);
      }
    }
  }

  private _bucketMap = new Map<number, Bucket<K, V>>();

  private _size = 0;

  get size(): number {
    return this._size;
  }

  * entries(): IterableIterator<[K, V]> {
    for (const [, bucket] of this._bucketMap) {
      if (bucket instanceof Set) {
        for (const entry of bucket) {
          yield entry;
        }
      } else {
        yield bucket;
      }
    }
  }

  iterate(): PeekIterator<[K, V]> {
    return new PeekIterator(this.entries());
  }

  get(key: K): V | undefined {
    const h = this._keyHash.hash(key);
    const bucket = this._bucketMap.get(h);

    if (bucket !== undefined) {
      // There is a bucket for the key's hash
      const entry = this._getFromBucket(bucket, key);
      if (entry !== undefined) {
        // There's an entry with this key
        const [, v] = entry;
        return v;
      } else {
        // There isn't an entry with this key (collision)
        return undefined;
      }
    } else {
      // There isn't a bucket for the key's hash
      return undefined;
    }
  }

  has(key: K): boolean {
    const h = this._keyHash.hash(key);
    const bucket = this._bucketMap.get(h);

    if (bucket !== undefined) {
      const entry = this._getFromBucket(bucket, key);
      return entry !== undefined;
    } else {
      return false;
    }
  }

  * keys(): IterableIterator<K> {
    for (const [k,] of this.entries()) {
      yield k;
    }
  }

  * values(): IterableIterator<V> {
    for (const [, v] of this.entries()) {
      yield v;
    }
  }

  clear(): void {
    this._bucketMap.clear();
    this._size = 0;
  }

  // Returns:
  // The old value, if it was present
  delete(key: K): V | undefined {
    const h = this._keyHash.hash(key);
    const bucket = this._bucketMap.get(h);

    if (bucket !== undefined) {
      // There was a bucket for the key's hash
      const entry = this._getFromBucket(bucket, key);

      if (entry !== undefined) {
        // The bucket contained the entry with this key

        const [, previousValue] = entry;

        if (bucket instanceof Set) {
          // It was a set-bucket

          if (bucket.size === 1) {
            // It was a last entry of the bucket, remove the bucket
            this._bucketMap.delete(h);
          } else {
            // It wasn't the last element of the bucket, remove the entry
            bucket.delete(entry);
          }

          this._size = this._size - 1;
          return previousValue;
        } else {
          // It was an entry-bucket, remove it
          this._bucketMap.delete(h);

          this._size = this._size - 1;
          return previousValue;
        }
      } else {
        // The bucket did not contain the entry with this key (only collisions!)
        return undefined;
      }
    } else {
      // There wasn't a bucket for that key
      return undefined;
    }

  }

  set(key: K, value: V): V | undefined {
    const h = this._keyHash.hash(key);

    const [keyWasAdded, previousValue] = this._add(h, key, value);

    if (keyWasAdded) {
      this._size = this._size + 1;
    }

    return previousValue;
  }

  _addToExistingSetBucket(key: K, value: V, setBucket: Set<[K, V]>): [boolean, V | undefined] {
    // It's a set-bucket
    const previousEntry = this._getFromSetBucket(setBucket, key);

    if (previousEntry !== undefined) {
      // The bucket contained entry with this key, replace that entry
      // TODO: Require V to be Equals?
      const wasRemoved = setBucket.delete(previousEntry);

      if (!wasRemoved) {
        throw new Error("Entry wasn't present in the bucket??");
      }

      setBucket.add([key, value]);

      const [, previousValue] = previousEntry;

      return [false, previousValue];
    } else {
      // Add a new entry to the bucket
      setBucket.add([key, value]);

      return [true, undefined];
    }
  }

  _addToExistingEntryBucket(h: number, key: K, value: V, previousEntry: [K, V]): [boolean, V | undefined] {
    // It was an entry-bucket

    const [entryKey, entryValue] = previousEntry;

    if (this._keyHash.isEqual(entryKey, key)) {
      // It's the same key, replace the entry-bucket

      this._bucketMap.set(h, new Set([
        [key, value],
      ]));

      return [false, entryValue];
    } else {
      // Promote the entry-bucket to a set-bucket

      this._bucketMap.set(h, new Set([
        previousEntry,
        [key, value],
      ]));

      return [true, entryValue];
    }
  }


  _addToExistingBucket(h: number, key: K, value: V, bucket: Bucket<K, V>): [boolean, V | undefined] {
    // There was a bucket for this key's hash...

    if (bucket instanceof Set) {
      return this._addToExistingSetBucket(key, value, bucket);
    } else {
      return this._addToExistingEntryBucket(h, key, value, bucket);
    }
  }

  _addToNewBucket(h: number, key: K, value: V): [boolean, V | undefined] {
    // There wasn't a bucket for that key's hash, create it

    this._bucketMap.set(h, [key, value]);

    return [true, undefined];
  }


  // Returns: whether the entry was actually added (i. e. key wasn't present
  // before)
  _add(h: number, key: K, value: V): [boolean, V | undefined] {
    const bucket = this._bucketMap.get(h);
    if (bucket !== undefined) {
      return this._addToExistingBucket(h, key, value, bucket);
    } else {
      return this._addToNewBucket(h, key, value);
    }
  }

  [Symbol.iterator](): IterableIterator<[K, V]> {
    return this.entries();
  }

  readonly [Symbol.toStringTag]: string = "HashMap";

  private _getFromBucket(bucket: Bucket<K, V>, searchedKey: K): [K, V] | undefined {
    if (bucket instanceof Set) {
      return this._getFromSetBucket(bucket, searchedKey);
    } else {
      const [key,] = bucket;
      if (this._keyHash.isEqual(key, searchedKey)) {
        return bucket;
      }
    }

    return undefined;
  }

  private _getFromSetBucket(bucket: Set<[K, V]>, searchedKey: K): [K, V] | undefined {
    for (const entry of bucket) {
      const [key,] = entry;
      if (this._keyHash.isEqual(key, searchedKey)) {
        return entry;
      }
    }
  }
}
