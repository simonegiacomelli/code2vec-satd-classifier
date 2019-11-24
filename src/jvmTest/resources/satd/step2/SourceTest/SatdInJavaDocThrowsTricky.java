class Class1 {
    /**
     * some comment
     *
     * @throws IOException If there is a problem opening the file representing
     *                     the bookmark BARF.
     */
    public void loadAutoBookmark() throws IOException {
        autoBookmark = Bookmark.getInstance(book.getPath());
        audioOffset = autoBookmark.getPosition();
        book.setCurrentIndex(autoBookmark.getNccIndex());
        book.goTo(book.current());
    }
}