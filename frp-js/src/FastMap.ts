import { Hash } from "./Hashable";

class Entry<K, V> {
    constructor(
        readonly key: K,
        readonly value: V,
    ) {
    }
}

export class FastMap<K, V> {
    constructor(
        private readonly _hashK: Hash<K>,
        private readonly _hashV: Hash<V>,
    ) {
        const map = new Map<number, Set<Entry<K, V>>>();

        let size = 0
        for (const bucket of map) {
            for (const entry of bucket) {
                size += 1;
            }
        }

        this._map = map;
        this.size = size;
    }

    private readonly _map: Map<number, Set<Entry<K, V>>>

    readonly size: number;

    containsKey(key: K): Boolean {
        const hashCode = this._hashK.hash(key);
        const bucket = this._map.get(hashCode);

        if (bucket !== undefined) {
            for (const e of bucket) {
                if (this._hashK.isEqual(key, e.key)) {
                    return true;
                }
            }
        }

        return false;
    }

    containsValue(value: V): Boolean {
        for (const [, bucket] of this._map) {
            for (const entry of bucket) {
                if (this._hashV.isEqual(value, entry.value)) {
                    return true;
                }
            }
        }

        return false;
    }

    get(key: K): V | null {
        const hashCode = this._hashK.hash(key);
        const bucket = this._map.get(hashCode);

        if (bucket !== undefined) {
            for (const e of bucket) {
                if (this._hashK.isEqual(key, e.key)) {
                    return e.value;
                }
            }
        }

        return null;
    }
}

class HashTableIterator<K, E> {
    private innerIterator: IterableIterator<E> | undefined = undefined;

    private peekedEntry: E | undefined = undefined;

    constructor(
        private readonly outerIterator: IterableIterator<[number, Set<E>]>,
    ) {
        this._peekOuter();
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
        const innerIterator = this.innerIterator;

        if (innerIterator !== undefined) {
            this._peekInner(innerIterator);
        } else {
            throw new Error("There's no inner iterator");
        }
    }

    private _peekInner(innerIterator: IterableIterator<E>) {
        const innerResult = innerIterator.next();

        if (innerResult.done) {
            return this._peekOuter();
        } else {
            this.peekedEntry = innerResult.value;
        }
    }

    private _peekOuter() {
        const outerResult = this.outerIterator.next();

        if (outerResult.done) {
            this.innerIterator = undefined;
            this.peekedEntry = undefined;
        } else {
            const [, bucket] = outerResult.value;
            const innerIterator = bucket[Symbol.iterator]();

            this.innerIterator = innerIterator;

            this._peekInner(innerIterator);
        }
    }
}

export class HashTable<K, E> {
    constructor(
        private readonly _hashK: Hash<K>,
        private readonly _extract: (e: E) => K,
    ) {
    }

    private readonly _map: Map<number, Set<E>> = new Map<number, Set<E>>();

    get size(): number {
        const map = this._map;

        let size = 0;

        for (const bucket of map) {
            for (const entry of bucket) {
                size += 1;
            }
        }

        return size;
    }

    get(key: K): E | null {
        const hashCode = this._hashK.hash(key);
        const bucket = this._map.get(hashCode);

        if (bucket !== undefined) {
            return this._getFromBucket(bucket, key);
        } else {
            return null;
        }
    }

    put(key: K, entry: E): E | null {
        const hashCode = this._hashK.hash(key);
        const bucket = this._map.get(hashCode);

        if (bucket !== undefined) {
            const existingEntry = this._getFromBucket(bucket, key);

            if (existingEntry) {
                bucket.delete(existingEntry);
            }

            bucket.add(entry);

            return existingEntry;
        } else {
            const bucket = new Set([entry]);

            this._map.set(hashCode, bucket);

            return null;
        }
    }

    clear() {
        this._map.clear();
    }

    iterate(): HashTableIterator<K, E> {
        const outerIterator = this._map[Symbol.iterator]();
        return new HashTableIterator<K, E>(outerIterator);
    }

    private _getFromBucket(bucket: Set<E>, key: K): E | null {
        for (const entry of bucket) {
            const entryKey = this._extract(entry)
            if (this._hashK.isEqual(key, entryKey)) {
                return entry;
            }
        }

        return null;
    }
}
