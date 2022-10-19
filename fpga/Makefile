#########################################################################################
# fpga prototype makefile
#########################################################################################

#########################################################################################
# general path variables
#########################################################################################
base_dir=$(abspath ..)
sim_dir=$(abspath .)

# do not generate simulation files
sim_name := none

#########################################################################################
# include shared variables
#########################################################################################
SUB_PROJECT ?= vcu118

ifeq ($(SUB_PROJECT),pgl22g)
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

include $(base_dir)/variables.mk

# default variables to build the arty example
# setup the board to use

.PHONY: default
default: $(mcs)

#########################################################################################
# misc. directories
#########################################################################################
fpga_dir := $(base_dir)/fpga/fpga-shells/$(FPGA_BRAND)
fpga_common_script_dir := $(fpga_dir)/common/tcl

#########################################################################################
# setup misc. sim files
#########################################################################################
SIM_FILE_REQS += \
	$(ROCKETCHIP_RSRCS_DIR)/vsrc/EICG_wrapper.v

# copy files but ignore *.h files in *.f (match vcs)
$(sim_files): $(SIM_FILE_REQS) | $(build_dir)
	cp -f $^ $(build_dir)
	$(foreach file,\
		$^,\
		$(if $(filter %.h,$(file)),\
			,\
			echo "$(addprefix $(build_dir)/, $(notdir $(file)))" >> $@;))

#########################################################################################
# import other necessary rules and variables
#########################################################################################
include $(base_dir)/common.mk

#########################################################################################
# copy from other directory
#########################################################################################
all_vsrcs := \
	$(sim_vsrcs) \
	$(base_dir)/generators/sifive-blocks/vsrc/SRLatch.v \
	$(fpga_dir)/common/vsrc/PowerOnResetFPGAOnly.v

#########################################################################################
# vivado rules
#########################################################################################
# combine all sources into single .f
synth_list_f := $(build_dir)/$(long_name).vsrcs.f
.PHONY: tcl_files
tcl_files: $(tcl_files_source)
	cp $(fpga_common_script_dir)/*.tcl $(build_dir)/
	cp $(fpga_dir)/$(BOARD)/tcl/*.tcl $(build_dir)/
	cd $(build_dir) && sed -i 's/\%top\%/$(MODEL)/g' *.tcl
	cd $(build_dir) && sed -i 's/\%long_name\%/$(long_name)/g' *.tcl
	cd $(build_dir) && sed -i 's#\%fpga_dir\%#$(fpga_dir)#g' *.tcl
fdc_files:
	cp $(fpga_dir)/$(BOARD)/constraints/*.fdc $(build_dir)
pds_ip_cores:
	-rm -rf $(build_dir)/ipcores
	-cp -r $(fpga_dir)/common/ipcores $(build_dir)

# .PHONY: $(build_dir)/mem.v
$(build_dir)/mem.v: $(build_dir)/$(long_name).top.mems.v
	cp $(fpga_dir)/common/vsrc/mem.v $(build_dir)/mem.v
	sed -i 's/module split_data_arrays_0_ext/module split_data_arrays_0_del/g' $(build_dir)/$(long_name).top.mems.v

$(build_dir)/FPGAStart.v: $(fpga_dir)/common/vsrc/FPGAStart.v
	cp $(fpga_dir)/common/vsrc/FPGAStart.v $(build_dir)/FPGAStart.v

ifneq ($(FPGA_BRAND),xilinx)
synth_list_f_req := $(sim_common_files) pds_ip_cores tcl_files $(build_dir)/mem.v $(all_vsrcs)
else
synth_list_f_req := $(sim_common_files) $(all_vsrcs)
endif
$(synth_list_f): $(synth_list_f_req)
	$(foreach file,$(all_vsrcs),echo "$(file)" >> $@;)
	cat $(sim_common_files) >> $@

ifneq ($(FPGA_BRAND),xilinx)
BIT_FILE := $(build_dir)/generate_bitstream/$(MODEL).sbit
EDA := pds_shell
EDA_ARGS := -f bitstream.tcl -project_name $(MODEL) -work_dir $(build_dir)
# EDA_ARGS := -f run_synth.tcl -project_name $(long_name) -work_dir $(build_dir)
EDA_ARGS_DEBUG := $(EDA_ARGS)
else
BIT_FILE := $(build_dir)/obj/$(MODEL).bit
EDA := vivado
EDA_ARGS := -nojournal -mode batch \
		-source $(fpga_common_script_dir)/vivado.tcl \
		-tclargs \
			-top-module "$(MODEL)" \
			-F "$(synth_list_f)" \
			-board "$(BOARD)" \
			-ip-vivado-tcls "$(shell find '$(build_dir)' -name '*.vivado.tcl')"
EDA_ARGS_DEBUG := -nojournal -mode batch \
		-source $(sim_dir)/scripts/run_impl_bitstream.tcl \
		-tclargs \
			$(build_dir)/obj/post_synth.dcp \
			$(BOARD) \
			$(build_dir)/debug_obj \
			$(fpga_common_script_dir)
endif
$(BIT_FILE): $(synth_list_f)
	cd $(build_dir) && $(EDA) $(EDA_ARGS)

.PHONY: bitstream
bitstream: $(BIT_FILE)

.PHONY: debug-bitstream
debug-bitstream: $(build_dir)/obj/post_synth.dcp
	cd $(build_dir); $(EDA) $(EDA_ARGS_DEBUG)

#########################################################################################
# general cleanup rules
#########################################################################################
.PHONY: clean
clean:
	rm -rf $(gen_dir)

.PHONY: test
test:
	echo $(MODEL)

.PHONY: show
show:
	python scripts/show_graph.py $(abspath $(build_dir)/$(long_name).graphml)