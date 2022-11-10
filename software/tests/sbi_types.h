/*
 * SPDX-License-Identifier: BSD-2-Clause
 *
 * Copyright (c) 2019 Western Digital Corporation or its affiliates.
 *
 * Authors:
 *   Anup Patel <anup.patel@wdc.com>
 */

#ifndef __SBI_TYPES_H__
#define __SBI_TYPES_H__

#ifndef OPENSBI_EXTERNAL_SBI_TYPES

/* clang-format off */

typedef char			s8;
typedef unsigned char		u8;
typedef unsigned char		uint8_t;

typedef short			s16;
typedef unsigned short		u16;
typedef short			int16_t;
typedef unsigned short		uint16_t;

typedef int			s32;
typedef unsigned int		u32;
typedef int			int32_t;
typedef unsigned int		uint32_t;

#ifndef __riscv_xlen
#define __riscv_xlen 32
#endif

#if __riscv_xlen == 64
typedef long			s64;
typedef unsigned long		u64;
typedef long			int64_t;
typedef unsigned long		uint64_t;
#define PRILX			"016lx"
#elif __riscv_xlen == 32
typedef long long		s64;
typedef unsigned long long	u64;
typedef long long		int64_t;
typedef unsigned long long	uint64_t;
#define PRILX			"08lx"
#else
#error "Unexpected __riscv_xlen"
#endif

typedef int			bool;
typedef unsigned long		ulong;
typedef unsigned long		uintptr_t;
typedef unsigned long		size_t;
typedef long			ssize_t;
typedef unsigned long		virtual_addr_t;
typedef unsigned long		virtual_size_t;
typedef unsigned long		physical_addr_t;
typedef unsigned long		physical_size_t;

#define TRUE			1
#define FALSE			0

#define NULL			((void *)0)

#define __packed		__attribute__((packed))
#define __noreturn		__attribute__((noreturn))

#define likely(x) __builtin_expect((x), 1)
#define unlikely(x) __builtin_expect((x), 0)

#undef offsetof
#ifdef __compiler_offsetof
#define offsetof(TYPE, MEMBER) __compiler_offsetof(TYPE,MEMBER)
#else
#define offsetof(TYPE, MEMBER) ((size_t) &((TYPE *)0)->MEMBER)
#endif

#define container_of(ptr, type, member) ({			\
	const typeof(((type *)0)->member) * __mptr = (ptr);	\
	(type *)((char *)__mptr - offsetof(type, member)); })

#define MAX(a, b) ((a) > (b) ? (a) : (b))
#define MIN(a, b) ((a) < (b) ? (a) : (b))
#define CLAMP(a, lo, hi) MIN(MAX(a, lo), hi)

#define STR(x) XSTR(x)
#define XSTR(x) #x

#define ROUNDUP(a, b) ((((a)-1) / (b) + 1) * (b))
#define ROUNDDOWN(a, b) ((a) / (b) * (b))

/* clang-format on */

#else
/* OPENSBI_EXTERNAL_SBI_TYPES could be defined in CFLAGS for using the
 * external definitions of data types and common macros.
 * OPENSBI_EXTERNAL_SBI_TYPES is the file name to external header file,
 * the external build system should address the additional include
 * directory ccordingly.
 */

#define XSTR(x) #x
#define STR(x) XSTR(x)
#include STR(OPENSBI_EXTERNAL_SBI_TYPES)
#endif


static inline void __raw_writeb(u8 val, volatile void *addr)
{
	asm volatile("sb %0, 0(%1)" : : "r"(val), "r"(addr));
}

static inline void __raw_writew(u16 val, volatile void *addr)
{
	asm volatile("sh %0, 0(%1)" : : "r"(val), "r"(addr));
}

static inline void __raw_writel(u32 val, volatile void *addr)
{
	asm volatile("sw %0, 0(%1)" : : "r"(val), "r"(addr));
}

#if __riscv_xlen != 32
static inline void __raw_writeq(u64 val, volatile void *addr)
{
	asm volatile("sd %0, 0(%1)" : : "r"(val), "r"(addr));
}
#endif

static inline u8 __raw_readb(const volatile void *addr)
{
	u8 val;

	asm volatile("lb %0, 0(%1)" : "=r"(val) : "r"(addr));
	return val;
}

static inline u16 __raw_readw(const volatile void *addr)
{
	u16 val;

	asm volatile("lh %0, 0(%1)" : "=r"(val) : "r"(addr));
	return val;
}

static inline u32 __raw_readl(const volatile void *addr)
{
	u32 val;

	asm volatile("lw %0, 0(%1)" : "=r"(val) : "r"(addr));
	return val;
}

#if __riscv_xlen != 32
static inline u64 __raw_readq(const volatile void *addr)
{
	u64 val;

	asm volatile("ld %0, 0(%1)" : "=r"(val) : "r"(addr));
	return val;
}
#endif

/* FIXME: These are now the same as asm-generic */

/* clang-format off */

#define __io_rbr()		do {} while (0)
#define __io_rar()		do {} while (0)
#define __io_rbw()		do {} while (0)
#define __io_raw()		do {} while (0)

#define readb_relaxed(c)	({ u8  __v; __io_rbr(); __v = __raw_readb(c); __io_rar(); __v; })
#define readw_relaxed(c)	({ u16 __v; __io_rbr(); __v = __raw_readw(c); __io_rar(); __v; })
#define readl_relaxed(c)	({ u32 __v; __io_rbr(); __v = __raw_readl(c); __io_rar(); __v; })

#define writeb_relaxed(v,c)	({ __io_rbw(); __raw_writeb((v),(c)); __io_raw(); })
#define writew_relaxed(v,c)	({ __io_rbw(); __raw_writew((v),(c)); __io_raw(); })
#define writel_relaxed(v,c)	({ __io_rbw(); __raw_writel((v),(c)); __io_raw(); })

#if __riscv_xlen != 32
#define readq_relaxed(c)	({ u64 __v; __io_rbr(); __v = __raw_readq(c); __io_rar(); __v; })
#define writeq_relaxed(v,c)	({ __io_rbw(); __raw_writeq((v),(c)); __io_raw(); })
#endif

#define __io_br()	do {} while (0)
#define __io_ar()	__asm__ __volatile__ ("fence i,r" : : : "memory");
#define __io_bw()	__asm__ __volatile__ ("fence w,o" : : : "memory");
#define __io_aw()	do {} while (0)

#define readb(c)	({ u8  __v; __io_br(); __v = __raw_readb(c); __io_ar(); __v; })
#define readw(c)	({ u16 __v; __io_br(); __v = __raw_readw(c); __io_ar(); __v; })
#define readl(c)	({ u32 __v; __io_br(); __v = __raw_readl(c); __io_ar(); __v; })

#define writeb(v,c)	({ __io_bw(); __raw_writeb((v),(c)); __io_aw(); })
#define writew(v,c)	({ __io_bw(); __raw_writew((v),(c)); __io_aw(); })
#define writel(v,c)	({ __io_bw(); __raw_writel((v),(c)); __io_aw(); })

#if __riscv_xlen != 32
#define readq(c)	({ u64 __v; __io_br(); __v = __raw_readq(c); __io_ar(); __v; })
#define writeq(v,c)	({ __io_bw(); __raw_writeq((v),(c)); __io_aw(); })
#endif


#endif
