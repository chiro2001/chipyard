ifeq ($(SUB_PROJECT),pgl22g)
	MODEL             ?= PGL22GTestHarness
	VLOG_MODEL        ?= PGL22GTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= PGL22GConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-vexriscv)
	MODEL             ?= PGL22GBareTestHarness
	VLOG_MODEL        ?= PGL22GBareTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= PGL22GVexRiscvConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),vexchip)
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
	SYN_TOP						?= VexChipTop
	CONSTRAINTS				?= vexchip
	MODEL							?= VexChip
	MODEL_PACKAGE     ?= vexriscv.demo
	CONFIG_PACKAGE    ?= vexriscv.demo
	GENERATOR_PACKAGE ?= chipyard
	VLOG_MODEL				?= VexChip
	CONFIG						?= GenVexChip
endif

ifeq ($(SUB_PROJECT),vexchip-debug)
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
	SYN_TOP						?= VexChipTop
	CONSTRAINTS				?= vexchip
	MODEL							?= VexChip
	MODEL_PACKAGE     ?= vexriscv.demo
	CONFIG_PACKAGE    ?= vexriscv.demo
	GENERATOR_PACKAGE ?= chipyard
	VLOG_MODEL				?= VexChip
	CONFIG						?= GenVexChipDebug
endif

ifeq ($(SUB_PROJECT),pgl22g-vexriscv-sim)
	MODEL             ?= PGL22GSimTestHarness
	VLOG_MODEL        ?= PGL22GSimTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= SimPGL22GVexRiscvConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-onchip-rocket-test)
	MODEL             ?= PGL22GOnChipTestHarness
	VLOG_MODEL        ?= PGL22GOnChipTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= PGL22GOnChipRocketTestsConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
	TOP								?= ChipTop
	SYN_TOP						?= PGL22GOnChipTestHarness
	MEM								?= mem_onchip.v
	CONSTRAINTS	 			?= onchip
endif

ifeq ($(SUB_PROJECT),pgl22g-onchip-rocket-test-small)
	MODEL             ?= PGL22GOnChipTestHarness
	VLOG_MODEL        ?= PGL22GOnChipTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= PGL22GOnChipRocketTestsSmallConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
	TOP								?= ChipTop
	SYN_TOP						?= PGL22GOnChipTestHarness
	MEM 							?= mem_onchip.v
	CONSTRAINTS	 			?= onchip
endif

ifeq ($(SUB_PROJECT),pgl22g-onchip-rocket-test-med)
	MODEL             ?= PGL22GOnChipTestHarness
	VLOG_MODEL        ?= PGL22GOnChipTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= PGL22GOnChipRocketTestsMedConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
	TOP								?= ChipTop
	SYN_TOP						?= PGL22GOnChipTestHarness
	MEM 							?= mem_onchip.v
	CONSTRAINTS	 			?= onchip
endif

ifeq ($(SUB_PROJECT),pgl22g-vexriscv2)
	MODEL             ?= PGL22GBareTestHarness
	VLOG_MODEL        ?= PGL22GBareTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= PGL22GVexRiscv2Config
	CONFIG_PACKAGE    ?= pgl22g.configs
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-onchip-vexriscv-test)
	MODEL             ?= PGL22GOnChipTestHarness
	VLOG_MODEL        ?= PGL22GOnChipTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= PGL22GOnChipVexRiscvTestsConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
	TOP								?= ChipTop
	SYN_TOP						?= PGL22GOnChipTestHarness
	MEM								?= mem_onchip.v
	CONSTRAINTS	 			?= onchip
endif

ifeq ($(SUB_PROJECT),pgl22g-picorv)
	MODEL             ?= PGL22GBareTestHarness
	VLOG_MODEL        ?= PGL22GBareTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= PGL22GPicoRVConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-ssrv)
	MODEL             ?= PGL22GBareTestHarness
	VLOG_MODEL        ?= PGL22GBareTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= PGL22GSSRVConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-bare)
	MODEL             ?= PGL22GBareTestHarness
	VLOG_MODEL        ?= PGL22GBareTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= PGL22GBareConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-tiny)
	MODEL             ?= PGL22GPerfTestHarness
	VLOG_MODEL        ?= PGL22GPerfTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= PGL22GTinyConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-sodor)
	MODEL             ?= PGL22GBareTestHarness
	VLOG_MODEL        ?= PGL22GBareTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= PGL22GSodorConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-sodor3)
	MODEL             ?= PGL22GBareTestHarness
	VLOG_MODEL        ?= PGL22GBareTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= PGL22GSodor3Config
	CONFIG_PACKAGE    ?= pgl22g.configs
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-sodoru)
	MODEL             ?= PGL22GBareTestHarness
	VLOG_MODEL        ?= PGL22GBareTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= PGL22GSodorUcodeConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-perf)
	MODEL             ?= PGL22GPerfTestHarness
	VLOG_MODEL        ?= PGL22GPerfTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= PGL22GPerfConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-xilinx)
	MODEL             ?= PGL22GFPGATestHarness
	VLOG_MODEL        ?= PGL22GFPGATestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= TinyRocketPGL22GConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= xilinx
endif

ifeq ($(SUB_PROJECT),vcu118)
	MODEL             ?= VCU118FPGATestHarness
	VLOG_MODEL        ?= VCU118FPGATestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.vcu118
	CONFIG            ?= RocketVCU118Config
	CONFIG_PACKAGE    ?= chipyard.fpga.vcu118
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= vcu118
	FPGA_BRAND        ?= xilinx
endif

ifeq ($(SUB_PROJECT),bringup)
	MODEL             ?= BringupVCU118FPGATestHarness
	VLOG_MODEL        ?= BringupVCU118FPGATestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.vcu118.bringup
	CONFIG            ?= RocketBringupConfig
	CONFIG_PACKAGE    ?= chipyard.fpga.vcu118.bringup
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= vcu118
	FPGA_BRAND        ?= xilinx
endif

ifeq ($(SUB_PROJECT),arty)
	# TODO: Fix with Arty
	MODEL             ?= ArtyFPGATestHarness
	VLOG_MODEL        ?= ArtyFPGATestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.arty
	CONFIG            ?= TinyRocketArtyConfig
	CONFIG_PACKAGE    ?= chipyard.fpga.arty
	GENERATOR_PACKAGE ?= chipyard
	BOARD             ?= arty
	FPGA_BRAND        ?= xilinx
endif

SBT_PROJECT ?= fpga_platforms
TB ?= none # unused
TOP ?= ChipTop
SYN_TOP ?= $(MODEL)
MEM ?= mem.v
CONSTRAINTS ?= ddr