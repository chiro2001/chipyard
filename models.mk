ifeq ($(SUB_PROJECT),pgl22g)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GTestHarness
	VLOG_MODEL        ?= PGL22GTestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.pgl22g
	CONFIG            ?= PGL22GConfig
	CONFIG_PACKAGE    ?= chipyard.fpga.pgl22g
	GENERATOR_PACKAGE ?= chipyard
	TB                ?= none # unused
	TOP               ?= ChipTop
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-picorv)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GBareTestHarness
	VLOG_MODEL        ?= PGL22GBareTestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.pgl22g
	CONFIG            ?= PGL22GPicoRVConfig
	CONFIG_PACKAGE    ?= chipyard.fpga.pgl22g
	GENERATOR_PACKAGE ?= chipyard
	TB                ?= none # unused
	TOP               ?= ChipTop
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-ssrv)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GBareTestHarness
	VLOG_MODEL        ?= PGL22GBareTestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.pgl22g
	CONFIG            ?= PGL22GSSRVConfig
	CONFIG_PACKAGE    ?= chipyard.fpga.pgl22g
	GENERATOR_PACKAGE ?= chipyard
	TB                ?= none # unused
	TOP               ?= ChipTop
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-bare)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GBareTestHarness
	VLOG_MODEL        ?= PGL22GBareTestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.pgl22g
	CONFIG            ?= PGL22GBareConfig
	CONFIG_PACKAGE    ?= chipyard.fpga.pgl22g
	GENERATOR_PACKAGE ?= chipyard
	TB                ?= none # unused
	TOP               ?= ChipTop
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-tiny)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GPerfTestHarness
	VLOG_MODEL        ?= PGL22GPerfTestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.pgl22g
	CONFIG            ?= PGL22GTinyConfig
	CONFIG_PACKAGE    ?= chipyard.fpga.pgl22g
	GENERATOR_PACKAGE ?= chipyard
	TB                ?= none # unused
	TOP               ?= ChipTop
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-sodor)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GBareTestHarness
	VLOG_MODEL        ?= PGL22GBareTestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.pgl22g
	CONFIG            ?= PGL22GSodorConfig
	CONFIG_PACKAGE    ?= chipyard.fpga.pgl22g
	GENERATOR_PACKAGE ?= chipyard
	TB                ?= none # unused
	TOP               ?= ChipTop
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-sodor3)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GBareTestHarness
	VLOG_MODEL        ?= PGL22GBareTestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.pgl22g
	CONFIG            ?= PGL22GSodor3Config
	CONFIG_PACKAGE    ?= chipyard.fpga.pgl22g
	GENERATOR_PACKAGE ?= chipyard
	TB                ?= none # unused
	TOP               ?= ChipTop
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-sodoru)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GBareTestHarness
	VLOG_MODEL        ?= PGL22GBareTestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.pgl22g
	CONFIG            ?= PGL22GSodorUcodeConfig
	CONFIG_PACKAGE    ?= chipyard.fpga.pgl22g
	GENERATOR_PACKAGE ?= chipyard
	TB                ?= none # unused
	TOP               ?= ChipTop
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-perf)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GPerfTestHarness
	VLOG_MODEL        ?= PGL22GPerfTestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.pgl22g
	CONFIG            ?= PGL22GPerfConfig
	CONFIG_PACKAGE    ?= chipyard.fpga.pgl22g
	GENERATOR_PACKAGE ?= chipyard
	TB                ?= none # unused
	TOP               ?= ChipTop
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= pango
endif

ifeq ($(SUB_PROJECT),pgl22g-xilinx)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GFPGATestHarness
	VLOG_MODEL        ?= PGL22GFPGATestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.pgl22g
	CONFIG            ?= TinyRocketPGL22GConfig
	CONFIG_PACKAGE    ?= chipyard.fpga.pgl22g
	GENERATOR_PACKAGE ?= chipyard
	TB                ?= none # unused
	TOP               ?= ChipTop
	BOARD             ?= pgl22g
	FPGA_BRAND        ?= xilinx
endif

ifeq ($(SUB_PROJECT),vcu118)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= VCU118FPGATestHarness
	VLOG_MODEL        ?= VCU118FPGATestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.vcu118
	CONFIG            ?= RocketVCU118Config
	CONFIG_PACKAGE    ?= chipyard.fpga.vcu118
	GENERATOR_PACKAGE ?= chipyard
	TB                ?= none # unused
	TOP               ?= ChipTop
	BOARD             ?= vcu118
	FPGA_BRAND        ?= xilinx
endif

ifeq ($(SUB_PROJECT),bringup)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= BringupVCU118FPGATestHarness
	VLOG_MODEL        ?= BringupVCU118FPGATestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.vcu118.bringup
	CONFIG            ?= RocketBringupConfig
	CONFIG_PACKAGE    ?= chipyard.fpga.vcu118.bringup
	GENERATOR_PACKAGE ?= chipyard
	TB                ?= none # unused
	TOP               ?= ChipTop
	BOARD             ?= vcu118
	FPGA_BRAND        ?= xilinx
endif

ifeq ($(SUB_PROJECT),arty)
	# TODO: Fix with Arty
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= ArtyFPGATestHarness
	VLOG_MODEL        ?= ArtyFPGATestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.arty
	CONFIG            ?= TinyRocketArtyConfig
	CONFIG_PACKAGE    ?= chipyard.fpga.arty
	GENERATOR_PACKAGE ?= chipyard
	TB                ?= none # unused
	TOP               ?= ChipTop
	BOARD             ?= arty
	FPGA_BRAND        ?= xilinx
endif