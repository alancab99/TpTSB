package soporte;

import java.io.Serializable;
import java.util.*;


public class TPUHashtable<K,V> implements Map<K,V>, Cloneable, Serializable
{
    // tamaño maximo del arreglo es y la tabla hash
    private final static int MAX_SIZE = Integer.MAX_VALUE;
    private Entry<K, V> table[];
    //para representar pares de objetos-> Entry k(clave)- v(valor)(clase interna)
        private class Entry<K, V> implements Map.Entry<K, V> {
            private K key;
            private V value;

            public Entry(K key, V value) {
                //Verificar si los parametros estan null
                if (key == null || value == null) {
                    throw new IllegalArgumentException(".. parámetro nulo");
                }

                this.key = key;
                this.value = value;
            }

            @Override
            public K getKey() {
                return key;
            }

            @Override
            public V getValue() {
                return value;
            }

            @Override
            public V setValue(V value) {
                if (value == null) {
                    throw new IllegalArgumentException("setValue(): parámetro null...");
                }
                V old = this.value;
                this.value = value;
                return old;
            }

            @Override
            public int hashCode() {
                int hash = 7;
                hash = 61 * hash + Objects.hashCode(this.key);
                hash = 61 * hash + Objects.hashCode(this.value);
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (this.getClass() != obj.getClass()) {
                    return false;
                }

                final Entry otro = (Entry) obj;
                if (!Objects.equals(this.key, otro.key)) {
                    return false;
                }
                if (!Objects.equals(this.value, otro.value)) {
                    return false;
                }
                return true;
            }

            @Override
            public String toString() {
                return "(" + key.toString() + ", " + value.toString() + ")";
            }
        }


    //*Mas atributos
    // tamaño con el que se creo la tabla y cantidad de objetos que contiene
    private int capacidad_inicial;
    private int count;
    /**
     * Estados
     * 0: Abierta
     * 1: Cerrada
     * 2: Tumba
     * **/
    private int states[];
    // lo usamos para verificar si hace falta rehashing -> "factor de carga"
    private float load_factor;
    //y aqui para las vistas junto con el fail fast iterator.
    private transient Set<K> keySet = null;
    private transient Set<Map.Entry<K,V>> entrySet = null;
    private transient Collection<V> values = null;
    protected transient int modCount;


    //se procede a crear la tabla vacia con capacidad inicial 5 y el facto r de carga 0.8f
    public TPUHashtable()
    {
        this(5, 0.8f);
    }


    public TPUHashtable(int capacidad_inicial, float load_factor)
    {
        if(load_factor <= 0) { load_factor = 0.8f; }
        if(capacidad_inicial <= 0) { capacidad_inicial = 11; }
        else
        {
            if(capacidad_inicial > TPUHashtable.MAX_SIZE)
            {
                capacidad_inicial = TPUHashtable.MAX_SIZE;
            } else {
                capacidad_inicial = this.siguientePrimo(capacidad_inicial);
            }
        this.table = new Entry[capacidad_inicial];

        states = new int[capacidad_inicial];

        this.capacidad_inicial = capacidad_inicial;
        this.load_factor = load_factor;
        this.count = 0;
        this.modCount = 0;

        }
    }

    // Map methods

    @Override
    public int size() {
        return this.count;
    }

    @Override
    public boolean isEmpty() {
        return (this.count == 0);
    }

    @Override
    public boolean containsKey(Object key) {
        return (this.get((K) key) != null);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.contains(value);
    }

    @Override
    public V get(Object key) {
        if (key == null)
            throw new NullPointerException("get(): parámetro null");

        int index = this.h((K)key);
        int j = 1;
        V valueReturn = null;

        // buscamos el elemento
        while (this.states[index] != 0) {

            if (this.states[index] == 1) {
                Entry<K, V> entry = this.table[index];

                // Si es el mismo devuelvo el value
                if(key.equals(entry.getKey())){
                    valueReturn = entry.getValue();
                    return valueReturn;
                }
            }
            // nuevo indice
            index += j * j;
            j++;
            if (index >= this.table.length) {
                index %= this.table.length;
            }
        }
        //null
        return valueReturn;
    }

    @Override
    public V put(K key, V value) {
        //Verificar parametros null
        if (key == null || value == null)
            throw new NullPointerException("put(): parámetro null");
        int index = this.h(key);
        int primera_tumba = -1;
        int j = 1;
        V viejo = null;

        // ..busco que no exista y guardar puntero a primera tumba
        while (this.states[index] != 0) {
            // ...si la posicion actual esta ocupada, verifico si es el mismo valor, usando equals()
            if (this.states[index] == 1) {
                Entry<K, V> entry = this.table[index];
                // ... es el mismo, sobreescribo y retorno el viejo
                if(key.equals(entry.getKey())){
                    viejo = entry.getValue();
                    entry.setValue(value);
                    this.count++;
                    this.modCount++;

                    return viejo;
                }
            }

            //puntero a tumba
            if(this.states[index] == 2 && primera_tumba < 0) primera_tumba = index;

            //nuevo indice
            index += j^2;
            j++;
            if (index >= this.table.length) {
                index %= this.table.length;
            }
        }

        if (primera_tumba >= 0) index = primera_tumba;

        // abierto o tumba?
        this.table[index] = new Entry<K, V>(key, value);
        this.states[index] = 1;

        // se actualiza contador
        this.count++;
        this.modCount++;

        // factor de carga verificado
        float fc = (float) count / (float) this.table.length;
        if (fc >= this.load_factor)
            this.rehash();

        return viejo;
    }

    @Override
    public V remove(Object key) {
        if (key == null)
            throw new NullPointerException("remove(): parámetro null");

        int index = this.h((K)key);
        int j = 1;
        V old = null;


        while (this.states[index] != 0) {

            // mismo value?
            if (this.states[index] == 1) {
                Entry<K, V> entry = this.table[index];

                // mismo lo elimino
                if(key.equals(entry.getKey())){
                    old = entry.getValue();
                    this.table[index] = null;
                    this.states[index] = 2;

                    this.count--;
                    this.modCount++;

                    return old;
                }
            }

            // indice
            index += j^2;
            j++;
            if (index >= this.table.length) {
                index %= this.table.length;
            }
        }

        // se devulve null a clave no asociada
        return old;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {

        this.table = new Entry[this.capacidad_inicial];

        states = new int[this.capacidad_inicial];

        this.count = 0;
        this.modCount++;
    }

    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            // keySet = Collections.synchronizedSet(new KeySet());
            keySet = new KeySet();
        }
        return keySet;
    }

    @Override
    public Collection<V> values() {
        if (values == null) {
            values = new ValueCollection();
        }
        return values;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (entrySet == null)
            entrySet = new EntrySet();
        return entrySet;
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        TPUHashtable<K, V> t = new TPUHashtable<>(this.table.length, this.load_factor);

        for(Map.Entry<K, V> entry : this.entrySet()){
            t.put(entry.getKey(), entry.getValue());
        }

        return t;
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Map)) {
            return false;
        }
        Map<K, V> t = (Map<K, V>) obj;
        if (t.size() != this.size()) {
            return false;
        }
        try {
            Iterator<Map.Entry<K, V>> i = this.entrySet.iterator();
            while (i.hasNext()) {
                Map.Entry<K, V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();
                if (t.get(key) == null) {
                    return false;
                } else {
                    if (!value.equals(t.get(key))) {
                        return false;
                    }
                }
            }
        }
        catch (ClassCastException e) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        if(this.isEmpty())
            return 0;

        return Arrays.hashCode(this.table);
    }

    public boolean contains(Object value) {
        if (value == null)
            return false;

        Iterator<Map.Entry<K, V>> it = this.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, V> entry = it.next();
            if (value.equals(entry.getValue()))
                return true;
        }

        return false;
    }

    protected void rehash()
    {
        int old_length = this.table.length;
        int new_length = siguientePrimo(old_length * 2 + 1);
        // por desborde...
        if(new_length > TPUHashtable.MAX_SIZE)
            new_length = TPUHashtable.MAX_SIZE;

        Entry<K, V> tempTable[] = new Entry[new_length];
        int tempStates[] = new int[new_length];

        for (int i = 0; i < tempStates.length; i++) tempStates[i] = 0;

        this.modCount++;

        for(int i = 0; i < this.table.length; i++){
            if(this.states[i] == 1){

                Entry<K, V> x = this.table[i];

                K key = x.getKey();
                int y = this.h(key, tempTable.length);
                int index = y, j = 1;


                while (tempStates[index] != 0) {
                    index += j * j;
                    j++;
                    if (index >= tempTable.length) {
                        index %= tempTable.length;
                    }
                }

                tempTable[index] = x;
                tempStates[index] = 1;
            }
        }

        this.table = tempTable;
        this.states = tempStates;
    }


    // KeySet de las clases
    private class KeySet extends AbstractSet<K> {
        @Override
        public Iterator<K> iterator() {
            return new KeySetIterator();
        }

        @Override
        public int size() {
            return TPUHashtable.this.count;
        }

        @Override
        public boolean contains(Object o) {
            return TPUHashtable.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return (TPUHashtable.this.remove(o) != null);
        }

        @Override
        public void clear() {
            TPUHashtable.this.clear();
        }
    private class KeySetIterator implements Iterator<K> {

            private int last_entry;

            private int current_entry;


            private boolean next_ok;

            private int expected_modCount;

            // Constructor
            public KeySetIterator() {
                last_entry = 0;
                current_entry = -1;
                next_ok = false;
                expected_modCount = TPUHashtable.this.modCount;
            }


            @Override
            public boolean hasNext() {
                Entry<K, V> t[] = TPUHashtable.this.table;
                int s[] = TPUHashtable.this.states;

                if(current_entry >= t.length) { return false; }

                int next_entry = current_entry + 1;
                for (int i = next_entry ; i < t.length; i++) {
                    if (s[i] == 1) return true;
                }

                return false;
            }


            @Override
            public K next() {
                // iterador
                if (TPUHashtable.this.modCount != expected_modCount) {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if (!hasNext()) {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                Entry<K, V> t[] = TPUHashtable.this.table;
                int s[] = TPUHashtable.this.states;

                int next_entry = current_entry;
                for (next_entry++ ; s[next_entry] != 1; next_entry++);

                last_entry = current_entry;
                current_entry = next_entry;

                next_ok = true;

                K key = t[current_entry].getKey();

                return key;
            }


            @Override
            public void remove() {
                // iterator fail-fast
                if (TPUHashtable.this.modCount != expected_modCount) {
                    throw new ConcurrentModificationException("evento inesperado...");
                }

                if (!next_ok) {
                    throw new IllegalStateException("next va antes de remove");
                }

                TPUHashtable.this.table[current_entry] = null;
                TPUHashtable.this.states[current_entry] = 2;

                current_entry = last_entry;

                next_ok = false;

                TPUHashtable.this.count--;

                TPUHashtable.this.modCount++;
                expected_modCount++;
            }
        }
    }

    // ...entrySet
    private class EntrySet extends AbstractSet<Map.Entry<K, V>> {

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntrySetIterator();
        }

        @Override
        public boolean contains(Object o) {
            if (o == null) {
                return false;
            }
            if (!(o instanceof TPUHashtable.Entry)) {
                return false;
            }
            Entry<K, V> t[] = TPUHashtable.this.table;
            int s[] = TPUHashtable.this.states;

            Entry<K, V> entry = (Entry<K, V>) o;

            int index = TPUHashtable.this.h(entry.getKey());
            int j = 1;

            while (s[index] != 0) {
                if (s[index] == 1) {
                    Entry<K, V> entryTable = t[index];

                    if(entryTable.equals(entry)) return true;
                }


                index += j^2;
                j++;
                if (index >= t.length) {
                    index %= t.length;
                }
            }

            return false;
        }


        @Override
        public boolean remove(Object o) {
            if (o == null) {
                throw new NullPointerException("parámetro nulo");
            }
            if (!(o instanceof TPUHashtable.Entry)) {
                return false;
            }

            Entry<K, V> t[] = TPUHashtable.this.table;
            int s[] = TPUHashtable.this.states;

            Entry<K, V> entry = (Entry<K, V>) o;


            int index = TPUHashtable.this.h(entry.getKey());

            int j = 1;

            while (s[index] != 0) {

                if (s[index] == 1) {
                    Entry<K, V> entryTable = t[index];

                    if(entryTable.equals(entry)){
                        t[index] = null;
                        s[index] = 2;

                        TPUHashtable.this.count--;
                        TPUHashtable.this.modCount++;

                        return true;
                    }
                }

                index += j^2;
                j++;
                if (index >= t.length) {
                    index %= t.length;
                }
            }
            //false si no hay clave asociada
            return false;
        }

        @Override
        public int size() {
            return TPUHashtable.this.count;
        }

        @Override
        public void clear() {
            TPUHashtable.this.clear();
        }

        private class EntrySetIterator implements Iterator<Map.Entry<K, V>> {
            private int last_entry;

            private int current_entry;

            private boolean next_ok;

            private int expected_modCount;


            public EntrySetIterator() {
                last_entry = 0;
                current_entry = -1;
                next_ok = false;
                expected_modCount = TPUHashtable.this.modCount;
            }


            @Override
            public boolean hasNext() {
                Entry<K, V> t[] = TPUHashtable.this.table;
                int s[] = TPUHashtable.this.states;

                if(current_entry >= t.length) { return false; }

                int next_entry = current_entry + 1;
                for (int i = next_entry ; i < t.length; i++) {
                    if (s[i] == 1) return true;
                }

                return false;
            }

            @Override
            public Entry<K, V> next() {
                if (TPUHashtable.this.modCount != expected_modCount) {
                    throw new ConcurrentModificationException("next(): evento inesperado");
                }

                if (!hasNext()) {
                    throw new NoSuchElementException("..no existe el elemento ...");
                }

                Entry<K, V> t[] = TPUHashtable.this.table;
                int s[] = TPUHashtable.this.states;

                int next_entry = current_entry;
                for (next_entry++ ; s[next_entry] != 1; next_entry++);

                last_entry = current_entry;
                current_entry = next_entry;

                next_ok = true;

                return t[current_entry];
            }


            @Override
            public void remove() {
                if (!next_ok) {
                    throw new IllegalStateException("...debe invocar a next() antes de remove()...");
                }

                TPUHashtable.this.table[current_entry] = null;
                TPUHashtable.this.states[current_entry] = 2;

                current_entry = last_entry;

                next_ok = false;

                TPUHashtable.this.count--;

                TPUHashtable.this.modCount++;
                expected_modCount++;
            }
        }
    }

    // ValueCollection
    private class ValueCollection extends AbstractCollection<V> {
        @Override
        public Iterator<V> iterator() {
            return new ValueCollectionIterator();
        }

        @Override
        public int size() {
            return TPUHashtable.this.count;
        }

        @Override
        public boolean contains(Object o) {
            return TPUHashtable.this.containsValue(o);
        }

        @Override
        public void clear() {
            TPUHashtable.this.clear();
        }

        private class ValueCollectionIterator implements Iterator<V> {
            private int last_entry;

            private int current_entry;

            private boolean next_ok;

            private int expected_modCount;


            public ValueCollectionIterator() {
                last_entry = 0;
                current_entry = -1;
                next_ok = false;
                expected_modCount = TPUHashtable.this.modCount;
            }


            @Override
            public boolean hasNext() {
                Entry<K, V> t[] = TPUHashtable.this.table;
                int s[] = TPUHashtable.this.states;

                if(current_entry >= t.length) { return false; }

                int next_entry = current_entry + 1;
                for (int i = next_entry ; i < t.length; i++) {
                    if (s[i] == 1) return true;
                }

                return false;
            }


            @Override
            public V next() {
                if (TPUHashtable.this.modCount != expected_modCount) {
                    throw new ConcurrentModificationException("next(): modificacion no esperada");
                }

                if (!hasNext()) {
                    throw new NoSuchElementException("next(): no existe el elemento");
                }

                Entry<K, V> t[] = TPUHashtable.this.table;
                int s[] = TPUHashtable.this.states;

                int next_entry = current_entry;
                for (next_entry++ ; s[next_entry] != 1; next_entry++);

                last_entry = current_entry;
                current_entry = next_entry;

                next_ok = true;

                V value = t[current_entry].getValue();

                return value;
            }


            @Override
            public void remove() {

                if (TPUHashtable.this.modCount != expected_modCount) {
                    throw new ConcurrentModificationException("...no se espero esta accion");
                }

                if (!next_ok) {
                    throw new IllegalStateException("....e debe invocar primero next() en remove");
                }

                TPUHashtable.this.table[current_entry] = null;
                TPUHashtable.this.states[current_entry] = 2;

                current_entry = last_entry;

                next_ok = false;

                TPUHashtable.this.count--;

                TPUHashtable.this.modCount++;
                expected_modCount++;
            }
        }
    }


     // las siguientes funciones hash por cada objeto enviado retorna el indice de esa clave para entrar a la tabla
     // y.
    private int h(int k) {
        return h(k, this.table.length);
    }

    private int h(K key) {
        return h(key.hashCode(), this.table.length);
    }
    //toma la clave y el tamaño t, y luego con el hascode retorna el indice para esa clave y tamaño
    private int h(K key, int t) {
        return h(key.hashCode(), t);
    }
    //en este caso es igual pero calcula y retorna el indice para esa clave
    private int h(int k, int t) {
        if (k < 0)
            k *= -1;
        return k % t;
    }

    //como vimos en lso desafios anteriores tomamos el siguiente primo:
    //junto con su funcion esPrimo
    public int siguientePrimo(int x) {
        while (!esPrimo(x)) {
            x ++;
        }
        return x;
    }

    private boolean esPrimo(int x) {
        if (x == 0 || x == 1 || x == 4) return false;
        for (int i = 2; i < x/2;i++) {
            if (x % i == 0) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder cad = new StringBuilder("");
        cad.append("\nTabla: {\n");
        for (int i = 0; i < this.table.length; i++) {
            if(this.table[i] == null){
                cad.append("\t()\n");
            }else{
                cad.append("\t").append(this.table[i].toString()).append("\n");
            }
        }
        cad.append("}");
        return cad.toString();
    }

}

