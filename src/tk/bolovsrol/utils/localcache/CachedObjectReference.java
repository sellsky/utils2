package tk.bolovsrol.utils.localcache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentMap;

class CachedObjectReference<I, O> extends SoftReference<O> {

    private final I id;
	private final ConcurrentMap<I, CachedObjectReference<I, O>> data;

	public CachedObjectReference(I id, ConcurrentMap<I, CachedObjectReference<I, O>> data, O referent, ReferenceQueue<Object> q) {
		super(referent, q);
        this.id = id;
        this.data = data;
    }


	public void discard() {
		data.remove(id, this);
		clear();
	}

}
