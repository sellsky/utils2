package tk.bolovsrol.utils.time;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringDumpBuilder;

import java.util.Date;

/**
 * Хранит упорядоченную последовательность объектов с определённой
 * для каждого объекта датой активизации.
 * <p/>
 * Внутри хранилища объекты представляют собой цепочку с указателем на последний
 * запрошенный элемент (пик).
 * <p/>
 * Когда запрашивается очередной элемент по дате, элементы перебираются от пика
 * в ближайшую к запрошенной дате сторону. Таким образом чем ближе расположены
 * запрашиваемые последовательно даты, тем быстрее реакция.
 */
public class DateContinuousStorage<T> {

    private Entry<T> first = null;
    private Entry<T> central = null;
    private Entry<T> last = null;

    public void add(T object, Date from) {
        Entry<T> newEntry = new Entry<T>(from, object);
        if (central == null) {
            first = newEntry;
            last = newEntry;
        } else if (central.date.before(newEntry.date)) {
            addAfter(central, newEntry);
        } else {
            addBefore(central, newEntry);
        }
        central = newEntry;
    }

    private void addAfter(Entry<T> oldEntry, Entry<T> newEntry) {
        while (true) {
            if (oldEntry.next == null) {
                // добавляем в конец
                oldEntry.next = newEntry;
                newEntry.prev = oldEntry;
                last = newEntry;
                return;
            } else {
                Entry<T> nextEntry = oldEntry.next;
                if (newEntry.date.before(nextEntry.date)) {
                    // вставляем между oldItem и nextItem
                    oldEntry.next = newEntry;
                    newEntry.prev = oldEntry;
                    newEntry.next = nextEntry;
                    nextEntry.prev = newEntry;
                    return;
                } else {
                    oldEntry = nextEntry;
                }
            }
        }
    }

    private void addBefore(Entry<T> oldEntry, Entry<T> newEntry) {
        while (true) {
            if (oldEntry.prev == null) {
                // добавляем в начало
                oldEntry.prev = newEntry;
                newEntry.next = oldEntry;
                first = newEntry;
                return;
            } else {
                Entry<T> prevEntry = oldEntry.prev;
                if (prevEntry.date.before(newEntry.date)) {
                    // вставляем между oldItem и nextItem
                    newEntry.prev = prevEntry;
                    prevEntry.next = newEntry;
                    oldEntry.prev = newEntry;
                    newEntry.next = oldEntry;
                    return;
                } else {
                    oldEntry = prevEntry;
                }
            }
        }
    }

    private Entry<T> getItem(Date when) {
        Entry<T> pick = central;
        if (!central.date.after(when)) {
            // возвращаем последующее
            while (true) {
                if (pick.next == null || when.before(pick.next.date)) {
                    break;
                } else {
                    pick = pick.next;
                }
            }
        } else {
            // возвращаем предыдущее
            while (true) {
                if (pick.prev == null) {
                    if (when.before(pick.date)) {
                        return null;
                    }
                    break;
                } else if (!pick.prev.date.after(when)) {
                    pick = pick.prev;
                    break;
                } else {
                    pick = pick.prev;
                }
            }
        }
        central = pick;
        return pick;
    }

    public Entry<T> getEntry(T payload) {
        Entry<T> entry = first;
        while (entry != null) {
            if (entry.value == payload) {
                return entry;
            }
            entry = entry.next;
        }
        return null;
    }

    public T get(Date when) {
        Entry<T> entry = getItem(when);
        return entry == null ? null : entry.value;
    }

    public boolean remove(T object) {
        Entry<T> entry = getEntry(object);
        if (entry == null) {
            return false;
        }
        remove(entry);
        return true;
    }

    private void remove(Entry<T> entry) {
        if (entry.prev == null) {
            if (entry.next == null) {
                // удаляем единственный элемент списка
                first = null;
                central = null;
                last = null;
            } else {
                // удаляем первый элемент списка
                first = entry.next;
                first.prev = null;
                if (central == entry) {
                    central = entry.next;
                }
            }
        } else if (entry.next == null) {
            // удаляем последний элемент списка
            if (central == entry) {
                central = entry.prev;
            }
            last = entry.prev;
            last.next = null;
        } else {
            // удаляем элемент из середины
            entry.prev.next = entry.next;
            entry.next.prev = entry.prev;
            if (central == entry) {
                central = entry.prev;
            }
        }
        entry.prev = null;
        entry.next = null;
    }

    public static class Entry<T> {
        Date date;
        T value;

        Entry<T> prev;
        Entry<T> next;

        Entry(Date date, T value) {
            this.date = date;
            this.value = value;
        }

        public Date getDate() {
            return date;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return new StringDumpBuilder()
                    .append("date", date)
                    .append("value", value)
                    .toString();
        }
    }

    public boolean isEmpty() {
        return central == null;
    }

    public T getFirst() {
        return first.value;
    }

    public Entry<T> getFirstEntry() {
        return first;
    }

    public Entry<T> removeFirstEntry() {
        Entry<T> entry = first;
        remove(first);
        return entry;
    }

    public T removeFirst() {
        T t = first.value;
        remove(first);
        return t;
    }

    public T getLast() {
        return last.value;
    }

    public Entry<T> getLastEntry() {
        return last;
    }

    public T removeLast() {
        T t = last.value;
        remove(last);
        return t;
    }

    public Entry<T> removeLastEntry() {
        Entry<T> entry = last;
        remove(last);
        return entry;
    }

//    public static void main(String[] args) throws Exception {
//        DateContinuousStorage<Date> x = new DateContinuousStorage<Date>();
//        GregorianCalendar gc = new GregorianCalendar();
//
//        Date d = gc.getTime();
//        x.add(d, d);
//
//        gc.add(Calendar.DATE, 1);
//        d = gc.getTime();
//        x.add(d, d);
//
//        gc.add(Calendar.DATE, 1);
//        d = gc.getTime();
//        x.add(d, d);
//
//        gc.add(Calendar.MONTH, -1);
//        d = gc.getTime();
//        x.add(d, d);
//
//        gc.add(Calendar.DATE, 1);
//        d = gc.getTime();
//        x.add(d, d);
//
//        gc.add(Calendar.DATE, -1);
//        gc.add(Calendar.MILLISECOND, +1);
//        System.out.println(Spell.get(x));
//    }

    @Override
    public String toString() {
        StringDumpBuilder sb = new StringDumpBuilder(" ", "~");
        Entry<T> entry = first;
        while (entry != null) {
            if (central == entry) {
                sb.append(">" + Spell.get(entry.date) + '<', Spell.get(entry.value));
            } else {
                sb.append(Spell.get(entry.date), Spell.get(entry.value));
            }
            entry = entry.next;
        }
        return sb.toString();
    }
}
