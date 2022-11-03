VEXCHIP_VERILOG := $(base_dir)/VexChip.v
VEXCHIP_DEBUG_VERILOG := $(base_dir)/VexChip.v
fpga_dir ?= $(base_dir)/fpga/fpga-shells/$(FPGA_BRAND)

.PHONY: build_dir_mk
build_dir_mk:
	-mkdir -p $(build_dir)

.PHONY: $(VEXCHIP_VERILOG)
$(VEXCHIP_VERILOG): build_dir_mk
	cd $(base_dir) && sbt -v "project VexRiscv; runMain vexriscv.demo.GenVexChip"
	cp $(VEXCHIP_VERILOG) $(build_dir)
	cp $(fpga_dir)/$(BOARD)/vsrc/VexChipTop.v $(build_dir)
	-touch $(build_dir)/empty.sv
	-mv $(base_dir)/*.bin $(build_dir)

.PHONY: $(VEXCHIP_DEBUG_VERILOG)_DEBUG
$(VEXCHIP_DEBUG_VERILOG)_DEBUG: build_dir_mk
	cd $(base_dir) && sbt -v "project VexRiscv; runMain vexriscv.demo.GenVexChipDebug"
	cp $(VEXCHIP_DEBUG_VERILOG) $(build_dir)
	cp $(fpga_dir)/$(BOARD)/vsrc/VexChipTopDebug.v $(build_dir)
	-touch $(build_dir)/empty.sv
	-mv $(base_dir)/*.bin $(build_dir)

.PHONY: vexchip-verilog-synth
vexchip-verilog-synth: build_dir_mk
	cd $(base_dir) && sbt -v "project VexRiscv; runMain vexriscv.demo.GenVexChipSynth"
	cp $(VEXCHIP_VERILOG) $(build_dir)
	cp $(fpga_dir)/$(BOARD)/vsrc/VexChipTop.v $(build_dir)
	-touch $(build_dir)/empty.sv
	-mv $(base_dir)/*.bin $(build_dir)

.PHONY: vexchip-bitstream
vexchip-bitstream: build_dir_mk vexchip-verilog-synth tcl_files
	cd $(build_dir) && $(EDA) $(EDA_ARGS)

.PHONY: vexchip-bitstream-debug
vexchip-bitstream-debug: build_dir_mk $(VEXCHIP_DEBUG_VERILOG)_DEBUG tcl_files
	cd $(build_dir) && $(EDA) $(EDA_ARGS)
