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

include $(base_dir)/models.mk

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
include $(base_dir)/extra.mk

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
	mkdir -p $(build_dir)
	cp $(fpga_common_script_dir)/*.tcl $(build_dir)/
	cp $(fpga_dir)/$(BOARD)/tcl/*.tcl $(build_dir)/
	cd $(build_dir) && sed -i 's/\%top\%/$(SYN_TOP)/g' *.tcl
	cd $(build_dir) && sed -i 's/\%long_name\%/$(long_name)/g' *.tcl
	cd $(build_dir) && sed -i 's#\%fpga_dir\%#$(fpga_dir)#g' *.tcl
	cd $(build_dir) && sed -i 's#\%vsrc_mem\%#$(MEM)#g' *.tcl
	cd $(build_dir) && sed -i 's#\%constraints\%#$(CONSTRAINTS)#g' *.tcl
	cd $(build_dir) && sed -i 's#\%ipcores\%#$(IPCORES)#g' *.tcl
fdc_files:
	cp $(fpga_dir)/$(BOARD)/constraints/*.fdc $(build_dir)
pds_ip_cores:
	-rm -rf $(build_dir)/ipcores
	-cp -r $(fpga_dir)/common/ipcores $(build_dir)

ifneq ($(FPGA_BRAND),xilinx)
synth_list_f_req := tcl_files
else
synth_list_f_req := 
endif

ifeq ($(IGNORE_VSRCS),)
synth_list_f_req += $(all_vsrcs)
synth_list_f_req += $(sim_common_files)
endif

$(synth_list_f): $(synth_list_f_req)
	$(foreach file,$(all_vsrcs),echo "$(file)" >> $@;)
	cat $(sim_common_files) >> $@

ifneq ($(FPGA_BRAND),xilinx)
BIT_FILE := $(build_dir)/generate_bitstream/$(MODEL).sbit
EDA := pds_shell
RUN_PROC ?= synth
RUN_TCL := run_$(RUN_PROC).tcl
EDA_ARGS := -f $(RUN_TCL) -project_name $(CONFIG)-$(MODEL) -work_dir $(build_dir)
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
	$(MAKE) -C $(abspath .) keep-rvfi
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

.PHONY: keep-rvfi
keep-rvfi:
	cd $(build_dir) && sed -i 's#^\s\+wire.\+rvfi.\+;#/* synthesis syn_keep=1 */&#g' *.v

include $(base_dir)/vex.mk

FLASH_BINARY ?= $(base_dir)/software/coremark/overlay/coremark.perf.bin
FLASH_MAKE ?= $(base_dir)/generators/vex-riscv/src/main/resources/bootrom
.PHONY: flash
flash:
	$(MAKE) -C $(FLASH_MAKE) clean
	$(MAKE) -C $(FLASH_MAKE)
	echo "cfg_gen_sfc -device_name W25Q128Q -opcode 11 -sbit_start_address 0 \
		-sbit $(build_dir)/generate_bitstream/$(MODEL).sbit -user_address_list { 00100000 } -file_list { $(FLASH_BINARY) } \
		-sfc $(build_dir)/generate_bitstream/$(MODEL).sfc; exit; " \
		| cdt_cfg_shell

.PHONY: vexsmp
vexsmp: tcl_files
	-$(MAKE) -C $(abspath .)  SUB_PROJECT=vexsmp verilog
	-touch $(build_dir)/$(long_name).top.mems.v
	-touch $(build_dir)/empty.sv
	cd $(build_dir) && $(EDA) $(EDA_ARGS)
