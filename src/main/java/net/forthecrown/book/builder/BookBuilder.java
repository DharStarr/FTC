package net.forthecrown.book.builder;

import com.google.common.base.Preconditions;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.Nullable;

public class BookBuilder {
    private static final Component NEW_LINE = Component.newline();

    public static final int PIXELS_PER_LINE = TextInfo.getPxWidth("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
    public static final int MAX_LINES = 13; // Can vary from 13 to 14, I don't know what it depends on

    TextComponent.Builder currentPage = Component.text();
    boolean pageAdded = false;
    boolean emptyPage = false;
    int pageCount = 0;
    int lineCount = 0;

    ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
    BookMeta bookMeta = (BookMeta) book.getItemMeta();

    public BookBuilder setTitle(String title) {
        bookMeta.setTitle(title);
        return this;
    }

    public BookBuilder setAuthor(String author) {
        bookMeta.setAuthor(author);
        return this;
    }

    public BookBuilder author(Component component) {
        bookMeta.author(component);
        return this;
    }

    public BookBuilder title(Component title) {
        bookMeta.title(title);
        return this;
    }

    public BookBuilder addEmptyLine() {
        return addText(Component.empty());
    }

    // Add text to current page
    public BookBuilder addText(Component line) {
        // Increase numLines according to new line length
        int extraLines = lineLength(line);
        Preconditions.checkState(extraLines <= MAX_LINES, "Text too big :(");

        // If numLines too big, paste new line on next page
        if (lineCount + extraLines > MAX_LINES) {
            addPage();
        }

        lineCount += extraLines;

        if (!emptyPage) {
            currentPage.append(NEW_LINE);
        }

        currentPage.append(line);

        pageAdded = false;
        emptyPage = false;

        return this;
    }

    public BookBuilder addCentered(Component text) {
        String strText = Text.plain(text);
        int pxLength = TextInfo.getPxWidth(strText);
        int dif = PIXELS_PER_LINE - pxLength;

        Validate.isTrue(dif >= 0, "Given text is longer than a single line");
        dif /= 2;

        return addText(
                Component.text()
                        .append(Component.text(TextInfo.getFiller(dif), NamedTextColor.WHITE))
                        .append(text)
                        .build()
        );
    }

    public static int lineLength(Component line) {
        String text = Text.plain(line);
        String[] lines = text.split("\n");

        int lineCount = lines.length;

        for (var s: lines) {
            int length = TextInfo.getPxWidth(s);
            int extraLines = length / PIXELS_PER_LINE;

            lineCount += extraLines;
        }

        return lineCount;
    }

    private void addPage() {
        bookMeta.addPages(currentPage.build());
        ++pageCount;
        pageAdded = true;
        emptyPage = true;

        currentPage = Component.text(); // empty page
        lineCount = 0; // No lines yet
    }

    public BookBuilder newPage() {
        addPage();
        return this;
    }

    public int getLineCount() {
        return lineCount;
    }

    public int getPageCount() {
        return pageCount;
    }

    public boolean canAddLine() {
        return canAddLines(1);
    }

    public boolean canAddLines(int lines) {
        int newLineCount = lineCount + lines;
        return newLineCount <= MAX_LINES;
    }

    public boolean hasTitle() {
        return bookMeta.hasTitle();
    }

    public boolean hasAuthor() {
        return bookMeta.hasAuthor();
    }

    @Nullable
    public String getAuthor() {
        return bookMeta.getAuthor();
    }

    public boolean hasGeneration() {
        return bookMeta.hasGeneration();
    }

    public BookMeta.@Nullable Generation getGeneration() {
        return bookMeta.getGeneration();
    }

    public void setGeneration(BookMeta.@Nullable Generation generation) {
        bookMeta.setGeneration(generation);
    }

    public boolean hasPages() {
        return bookMeta.hasPages();
    }

    @Nullable
    public Component author() {
        return bookMeta.author();
    }

    public BookMeta build() {
        if (!pageAdded) {
            addPage();
        }

        return bookMeta.clone();
    }
}