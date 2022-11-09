__STACKSIZE__ = 0x1000;

ENTRY(_start)
SECTIONS
{
    ROM_BASE = 0x10000;
    . = ROM_BASE;

    .start :
    {
        *(.start);
    }

    . = ALIGN(4);
    . = ROM_BASE + 0x80;
    .text.hang : { *(.text.hang) }

    .text : 
    {
        *(.text)                        /* remaining code */
        *(.text.*)                      /* remaining code */
        *(.rodata)                      /* read-only data (constants) */
        *(.rodata*)
    }

    .data : 
    {
        *(.data)
        *(.data.*)
    
        *(.data1)
        *(.data1.*)

        . = ALIGN(4);
        PROVIDE( __global_pointer$ = . + 0x800 );

        *(.sdata)
        *(.sdata.*)
    }

    .stack :
    {
        . = ALIGN(64);
        __stack_start__ = .;

        . += __STACKSIZE__;
        __stack_cpu0 = .;
    }

    .sbss : 
    {
    __bss_start = .;
        *(.sbss)
        *(.sbss.*)
        *(.dynsbss)
        *(.scommon)
    }

    .bss :
    {
        *(.bss)
        *(.bss.*)
        *(.dynbss)
        *(COMMON)
    __bss_end = .;
    }

    _end = .;
}
