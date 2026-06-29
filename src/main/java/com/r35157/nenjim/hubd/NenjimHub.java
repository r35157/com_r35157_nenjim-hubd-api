package com.r35157.nenjim.hubd;

import com.r35157.nenjim.hubd.journal.Journal;
import org.jetbrains.annotations.NotNull;

public interface NenjimHub {
    /**
     * A no-operation (noop). This method is suppoted to do nothing.
     */
    void noop();

    /**
     *
     * @param journal
     */
    void monitorJournal(@NotNull Journal journal);
}

