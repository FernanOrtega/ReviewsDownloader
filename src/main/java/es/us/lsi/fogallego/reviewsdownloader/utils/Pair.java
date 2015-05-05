package es.us.lsi.fogallego.reviewsdownloader.utils;

/**
 * @author mamuso
 *         <p/>
 *         Immutable
 *         For those missing the C++ Pair{@literal< A , B >}
 *         Do not use this if semantics relationship is needed i.e. use it
 *         only if the pair is to be used as an auxiliary and has no meaning.
 * @see <a href="http://stackoverflow.com/a/677248" > this stackoverflow post</a>
 */
public class Pair<A, B> implements Comparable<Pair<A, B>> {
    private final A mFirst;
    private final B mSecond;

    /**
     * dummy constructor for Persistence libraries (as XStream or GWT for example)
     */
    @SuppressWarnings("unused")
    private Pair() {
        mFirst = null;
        mSecond = null;
    }

    /**
     * Preferred constructor
     *
     * @param pFirst  first member of the pair
     * @param pSecond second member of the pair
     */
    public Pair(A pFirst, B pSecond) {
        super();
        this.mFirst = pFirst;
        this.mSecond = pSecond;
    }

    public int hashCode() {
        int hashFirst = mFirst != null ? mFirst.hashCode() : 0;
        int hashSecond = mSecond != null ? mSecond.hashCode() : 0;

        return hashFirst + 31 * hashSecond;
    }

    public boolean equals(Object pOther) {
        if (pOther instanceof Pair<?, ?>) {
            @SuppressWarnings("unchecked")
            Pair<?, ?> otherPair = (Pair<A, B>) pOther;
            return (((mFirst == null && otherPair.mFirst == null) ||
                    (mFirst != null && otherPair.mFirst != null &&
                            mFirst.equals(otherPair.mFirst))) &&
                    ((mSecond == null && otherPair.mSecond == null) ||
                            (mSecond != null && otherPair.mSecond != null &&
                                    mSecond.equals(otherPair.mSecond))));
        }

        return false;
    }

    public String toString() {
        return "(" + mFirst + ", " + mSecond + ")";
    }

    public A getFirst() {
        return mFirst;
    }

    public B getSecond() {
        return mSecond;
    }

    @SuppressWarnings("unchecked")
    public int compareTo(Pair<A, B> pOther) {
        int result;
        try {
            result = ((Comparable<A>) mFirst).compareTo(pOther.getFirst());
            if (result == 0) {
                result = ((Comparable<B>) mSecond).compareTo(pOther.getSecond());
            }
        } catch (ClassCastException e) {
            result = 0;
        }
        return result;
    }

}
