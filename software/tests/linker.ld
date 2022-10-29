SECTIONS
{
    ROM_BASE = 0x10000; /* ... but actually position independent */

    . = ROM_BASE;
    .text._start : { *(.text._start) }
    . = ROM_BASE + 0x40;
    .text.reset_vec : { *(.text.reset_vec) }
}
