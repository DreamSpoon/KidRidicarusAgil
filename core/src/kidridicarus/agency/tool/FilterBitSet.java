package kidridicarus.agency.tool;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/*
 * Title: Contact Filter (CF) Bit Set
 * Desc: A collection of contact filter bits enumerated by String, no duplicates allowed.
 */
public class FilterBitSet {
	private HashSet<String> bits;

	public FilterBitSet() {
		bits = new HashSet<String>();
	}

	public FilterBitSet(String ...bitsInput) {
		bits = new HashSet<String>();
		for(String bit : bitsInput)
			bits.add(bit);
	}

	public FilterBitSet(Collection<String> bitsInput) {
		bits = new HashSet<String>(bitsInput);
	}

	public FilterBitSet and(String ...otherBits) {
		return and(new FilterBitSet(otherBits));
	}

	// bitwise AND operation
	public FilterBitSet and(FilterBitSet otherSet) {
		// create a new empty set
		HashSet<String> resultBits = new HashSet<String>();
		for(String otherBit : otherSet.bits) {
			// add bit from other sequence only if the bit exists in this sequence
			if(bits.contains(otherBit))
				resultBits.add(otherBit);
		}
		// return result of 'AND' operation
		return new FilterBitSet(resultBits);
	}

	public FilterBitSet or(String ...bitsInput) {
		return or(new FilterBitSet(bitsInput));
	}

	// bitwise INCLUSIVE OR operation
	public FilterBitSet or(FilterBitSet otherSet) {
		// create a new empty set, then add all bits from this set to new set
		HashSet<String> resultBits = new HashSet<String>(bits);
		// add all bits from other set to result set, duplicates are ignored
		resultBits.addAll(otherSet.bits);
		// return result of 'INCLUSIVE OR' operation
		return new FilterBitSet(resultBits);
	}

	public void setZero() {
		bits.clear();
	}

	public boolean isZero() {
		return bits.isEmpty();
	}

	public boolean isNonZero() {
		return !bits.isEmpty();
	}

	@Override
	public boolean equals(Object otherSeq) {
		if(!(otherSeq instanceof FilterBitSet))
			return false;

		// if the other bit sequence has a different number of bits than this sequence then they are not equal
		if(bits.size() != ((FilterBitSet) otherSeq).bits.size())
			return false;
		// check each bit against the other for differences, return false if any difference found
		for(String otherBit : ((FilterBitSet) otherSeq).bits)
			if(!bits.contains(otherBit))
				return false;
		// no differences found, return true
		return true;
	}

	@Override
	public String toString() {
		Iterator<String> bitsIter = bits.iterator();
		StringBuilder strb = new StringBuilder("[\"");
		while(bitsIter.hasNext()) {
			strb.append(bitsIter.next());
			if(bitsIter.hasNext())
				strb.append("\", \"");
		}
		return strb.append("\"]").toString();
	}
}
