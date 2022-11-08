ifeq ($(SUB_PROJECT),vexchip)
	SBT_PROJECT			 	?= VexRiscv
	MODEL 						?= VexChip
	VLOG_MODEL 				?= VexChip
	MODEL_PACKAGE 		?= vexriscv.demo
	CONFIG 						?= GenVexChip
	CONFIG_PACKAGE 		?= vexriscv.demo
endif
ifeq ($(SUB_PROJECT),vexchip-debug)
	SBT_PROJECT			 	?= VexRiscv
	MODEL 						?= VexChip
	VLOG_MODEL 				?= VexChip
	MODEL_PACKAGE 		?= vexriscv.demo
	CONFIG 						?= GenVexChip
	CONFIG_PACKAGE 		?= vexriscv.demo
endif
ifeq ($(SUB_PROJECT),default)
	SBT_PROJECT			 	?= chipyard
	MODEL 						?= TestHarness
	VLOG_MODEL 				?= TestHarness
	MODEL_PACKAGE 		?= chipyard
	CONFIG 						?= RocketConfig
	CONFIG_PACKAGE 		?= chipyard
endif
ifeq ($(SUB_PROJECT),default-tiny)
	SBT_PROJECT			 	?= chipyard
	MODEL 						?= TestHarness
	VLOG_MODEL 				?= TestHarness
	MODEL_PACKAGE 		?= chipyard
	CONFIG 						?= TinyRocketConfig
	CONFIG_PACKAGE 		?= chipyard
endif
ifeq ($(SUB_PROJECT),default32-small)
	SBT_PROJECT			 	?= fpga_platforms
	MODEL 						?= TestHarness
	VLOG_MODEL 				?= TestHarness
	MODEL_PACKAGE 		?= chipyard
	CONFIG 						?= RocketSmall32Config
	CONFIG_PACKAGE 		?= pgl22g.configs
endif
ifeq ($(SUB_PROJECT),default-cva6)
	SBT_PROJECT			 	?= chipyard
	MODEL 						?= TestHarness
	VLOG_MODEL 				?= TestHarness
	MODEL_PACKAGE 		?= chipyard
	CONFIG 						?= CVA6Config
	CONFIG_PACKAGE 		?= chipyard
endif
ifeq ($(SUB_PROJECT),default-cva6dmi)
	SBT_PROJECT			 	?= chipyard
	MODEL 						?= TestHarness
	VLOG_MODEL 				?= TestHarness
	MODEL_PACKAGE 		?= chipyard
	CONFIG 						?= dmiCVA6Config
	CONFIG_PACKAGE 		?= chipyard
endif
ifeq ($(SUB_PROJECT),pgl22g-vexriscv)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GSimTestHarness
	VLOG_MODEL        ?= PGL22GSimTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= SimPGL22GVexRiscvConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
endif
ifeq ($(SUB_PROJECT),vexsmp)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= VexChipSmpWrapper
	VLOG_MODEL        ?= VexChipSmpWrapper
	MODEL_PACKAGE     ?= pgl22g.simple
	CONFIG            ?= VexChipSmpConfig
	CONFIG_PACKAGE    ?= pgl22g.simple
	TOP 							?= VexChipSmpWrapper
endif
ifeq ($(SUB_PROJECT),pgl22g-vexriscv-n)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GSimTestHarness
	VLOG_MODEL        ?= PGL22GSimTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= SimPGL22GVexRiscvNConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
endif
ifeq ($(SUB_PROJECT),pgl22g-vexriscv-spi)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GSimTestHarness
	VLOG_MODEL        ?= PGL22GSimTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= SimPGL22GVexRiscvSpiConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
endif
ifeq ($(SUB_PROJECT),pgl22g-onchip)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GSimTestHarness
	VLOG_MODEL        ?= PGL22GSimTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= SimPGL22GOnChipRocketConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
endif
ifeq ($(SUB_PROJECT),pgl22g-onchip-rocket-sim-small)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GSimTestHarness
	VLOG_MODEL        ?= PGL22GSimTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= SimPGL22GOnChipRocketSmallConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
endif
ifeq ($(SUB_PROJECT),pgl22g-onchip-rocket-test-med)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GSimTestHarness
	VLOG_MODEL        ?= PGL22GSimTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= PGL22GOnChipRocketTestsMedConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
endif
ifeq ($(SUB_PROJECT),pgl22g-onchip-rocket-test)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GSimTestHarness
	VLOG_MODEL        ?= PGL22GSimTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= PGL22GOnChipRocketTestsConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
endif
ifeq ($(SUB_PROJECT),pgl22g-onchip-vexriscv-test)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GSimTestHarness
	VLOG_MODEL        ?= PGL22GSimTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= PGL22GOnChipVexRiscvTestsConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
endif
ifeq ($(SUB_PROJECT),pgl22g-onchip-coremark)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GSimTestHarness
	VLOG_MODEL        ?= PGL22GSimTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= PGL22GOnChipRocketCoreMarkConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
endif
ifeq ($(SUB_PROJECT),pgl22g-ssrv)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GSimTestHarness
	VLOG_MODEL        ?= PGL22GSimTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= SimPGL22GSSRVConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
endif
ifeq ($(SUB_PROJECT),pgl22g-picorv)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GSimTestHarness
	VLOG_MODEL        ?= PGL22GSimTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= SimPGL22GPicoRVConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
endif
ifeq ($(SUB_PROJECT),pgl22g)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GSimTestHarness
	VLOG_MODEL        ?= PGL22GSimTestHarness
	MODEL_PACKAGE     ?= pgl22g.testharness
	CONFIG            ?= SimTinyRocketPGL22GConfig
	CONFIG_PACKAGE    ?= pgl22g.configs
endif
ifeq ($(SUB_PROJECT),sodor)
	SBT_PROJECT			 	?= chipyard
	MODEL 						?= TestHarness
	VLOG_MODEL 				?= TestHarness
	MODEL_PACKAGE 		?= chipyard
	CONFIG 						?= Sodor5StageConfig
	CONFIG_PACKAGE 		?= chipyard
endif

GENERATOR_PACKAGE ?= chipyard
TB ?= TestDriver
TOP ?= ChipTop
BOARD ?= pgl22g
FPGA_BRAND ?= pango