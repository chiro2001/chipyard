ifeq ($(SUB_PROJECT),default)
	SBT_PROJECT			 	?= chipyard
	MODEL 						?= TestHarness
	VLOG_MODEL 				?= TestHarness
	MODEL_PACKAGE 		?= chipyard
	CONFIG 						?= RocketConfig
	CONFIG_PACKAGE 		?= chipyard
	GENERATOR_PACKAGE ?= chipyard
	TB 								?= TestDriver
	TOP 							?= ChipTop
	BOARD 						?= 
	FPGA_BRAND 				?= 
endif
ifeq ($(SUB_PROJECT),default-tiny)
	SBT_PROJECT			 	?= chipyard
	MODEL 						?= TestHarness
	VLOG_MODEL 				?= TestHarness
	MODEL_PACKAGE 		?= chipyard
	CONFIG 						?= TinyRocketConfig
	CONFIG_PACKAGE 		?= chipyard
	GENERATOR_PACKAGE ?= chipyard
	TB 								?= TestDriver
	TOP 							?= ChipTop
	BOARD 						?= 
	FPGA_BRAND 				?= 
endif
ifeq ($(SUB_PROJECT),default32)
	SBT_PROJECT			 	?= fpga_platforms
	MODEL 						?= TestHarness
	VLOG_MODEL 				?= TestHarness
	MODEL_PACKAGE 		?= chipyard
	CONFIG 						?= RocketBig32Config
	CONFIG_PACKAGE 		?= chipyard.fpga.pgl22g
	GENERATOR_PACKAGE ?= chipyard
	TB 								?= TestDriver
	TOP 							?= ChipTop
	BOARD 						?= 
	FPGA_BRAND 				?= 
endif
ifeq ($(SUB_PROJECT),default32-small)
	SBT_PROJECT			 	?= fpga_platforms
	MODEL 						?= TestHarness
	VLOG_MODEL 				?= TestHarness
	MODEL_PACKAGE 		?= chipyard
	CONFIG 						?= RocketSmall32Config
	CONFIG_PACKAGE 		?= chipyard.fpga.pgl22g
	GENERATOR_PACKAGE ?= chipyard
	TB 								?= TestDriver
	TOP 							?= ChipTop
	BOARD 						?= 
	FPGA_BRAND 				?= 
endif
ifeq ($(SUB_PROJECT),default-cva6)
	SBT_PROJECT			 	?= chipyard
	MODEL 						?= TestHarness
	VLOG_MODEL 				?= TestHarness
	MODEL_PACKAGE 		?= chipyard
	CONFIG 						?= CVA6Config
	CONFIG_PACKAGE 		?= chipyard
	GENERATOR_PACKAGE ?= chipyard
	TB 								?= TestDriver
	TOP 							?= ChipTop
	BOARD 						?= 
	FPGA_BRAND 				?= 
endif
ifeq ($(SUB_PROJECT),default-cva6dmi)
	SBT_PROJECT			 	?= chipyard
	MODEL 						?= TestHarness
	VLOG_MODEL 				?= TestHarness
	MODEL_PACKAGE 		?= chipyard
	CONFIG 						?= dmiCVA6Config
	CONFIG_PACKAGE 		?= chipyard
	GENERATOR_PACKAGE ?= chipyard
	TB 								?= TestDriver
	TOP 							?= ChipTop
	BOARD 						?= 
	FPGA_BRAND 				?= 
endif
ifeq ($(SUB_PROJECT),pgl22g-vexriscv)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GSimTestHarness
	VLOG_MODEL        ?= PGL22GSimTestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.pgl22g
	CONFIG            ?= SimPGL22GVexRiscvConfig
	CONFIG_PACKAGE    ?= chipyard.fpga.pgl22g
	GENERATOR_PACKAGE ?= chipyard
	TB                ?= TestDriver
	TOP               ?= ChipTop
	BOARD             ?= 
	FPGA_BRAND        ?= 
endif
ifeq ($(SUB_PROJECT),pgl22g-onchip)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GSimTestHarness
	VLOG_MODEL        ?= PGL22GSimTestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.pgl22g
	CONFIG            ?= SimPGL22GOnChipRocketConfig
	CONFIG_PACKAGE    ?= chipyard.fpga.pgl22g
	GENERATOR_PACKAGE ?= chipyard
	TB                ?= TestDriver
	TOP               ?= ChipTop
	BOARD             ?= 
	FPGA_BRAND        ?= 
endif
ifeq ($(SUB_PROJECT),pgl22g-ssrv)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GSimTestHarness
	VLOG_MODEL        ?= PGL22GSimTestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.pgl22g
	CONFIG            ?= SimPGL22GSSRVConfig
	CONFIG_PACKAGE    ?= chipyard.fpga.pgl22g
	GENERATOR_PACKAGE ?= chipyard
	TB                ?= TestDriver
	TOP               ?= ChipTop
	BOARD             ?= 
	FPGA_BRAND        ?= 
endif
ifeq ($(SUB_PROJECT),pgl22g-picorv)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GSimTestHarness
	VLOG_MODEL        ?= PGL22GSimTestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.pgl22g
	CONFIG            ?= SimPGL22GPicoRVConfig
	CONFIG_PACKAGE    ?= chipyard.fpga.pgl22g
	GENERATOR_PACKAGE ?= chipyard
	TB                ?= TestDriver
	TOP               ?= ChipTop
	BOARD             ?= 
	FPGA_BRAND        ?= 
endif
ifeq ($(SUB_PROJECT),pgl22g)
	SBT_PROJECT       ?= fpga_platforms
	MODEL             ?= PGL22GSimTestHarness
	VLOG_MODEL        ?= PGL22GSimTestHarness
	MODEL_PACKAGE     ?= chipyard.fpga.pgl22g
	CONFIG            ?= SimTinyRocketPGL22GConfig
	CONFIG_PACKAGE    ?= chipyard.fpga.pgl22g
	GENERATOR_PACKAGE ?= chipyard
	TB                ?= TestDriver
	TOP               ?= ChipTop
	BOARD             ?= 
	FPGA_BRAND        ?= 
endif
ifeq ($(SUB_PROJECT),sodor)
	SBT_PROJECT			 	?= chipyard
	MODEL 						?= TestHarness
	VLOG_MODEL 				?= TestHarness
	MODEL_PACKAGE 		?= chipyard
	CONFIG 						?= Sodor5StageConfig
	CONFIG_PACKAGE 		?= chipyard
	GENERATOR_PACKAGE ?= chipyard
	TB 								?= TestDriver
	TOP 							?= ChipTop
	BOARD 						?= 
	FPGA_BRAND 				?= 
endif
