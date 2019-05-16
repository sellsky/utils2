package tk.bolovsrol.utils.store;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.log.LogDome;

import java.util.Map;

public class StoreHelper {

    private StoreHelper() {
    }

    public static void forceStoreAllAndLogErrors(LogDome log, StoreManager storeManager) {
        Map<String, StoreException> stringStoreExceptionMap = storeManager.forceStoreAll();
        if (stringStoreExceptionMap != null) {
            for (Map.Entry<String, StoreException> entry : stringStoreExceptionMap.entrySet()) {
                log.warning("Store failed for storeable " + Spell.get(entry.getKey()) + ". " + Spell.get(entry.getValue()));
            }
        }
    }
}
