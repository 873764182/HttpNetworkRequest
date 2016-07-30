package com.panxiong.http.cookie;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import okhttp3.Cookie;

public class SetCookieCache implements CookieCache {

    private Set<IdentifiableCookie> cookies;

    public SetCookieCache() {
        cookies = new HashSet<>();
    }

    @Override
    public void addAll(Collection<Cookie> newCookies) {
        updateCookies(IdentifiableCookie.decorateAll(newCookies));
    }

    /**
     * All cookies will be added to the collection, already existing cookies will be overwritten by the new ones.
     *
     * @param cookies
     */
    private void updateCookies(Collection<IdentifiableCookie> cookies) {
        this.cookies.removeAll(cookies);
        this.cookies.addAll(cookies);
    }

    @Override
    public void clear() {
        cookies.clear();
    }

    @Override
    public Iterator<Cookie> iterator() {
        return new SetCookieCacheIterator();
    }

    private class SetCookieCacheIterator implements Iterator<Cookie> {

        private Iterator<IdentifiableCookie> iterator;

        public SetCookieCacheIterator() {
            iterator = cookies.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Cookie next() {
            return iterator.next().getCookie();
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }
}
