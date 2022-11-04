## chipyard-new 说明

### 配置

仿真配置文件：`models-sim.mk`

综合配置文件：`models.mk`

配置参数说明：

| SBT_PROJECT    | 需要生成的配置在哪个 sbt 项目中。Chipyard默认配置一般在 `chipyard`，VexRiscv、RocketChip 等配置在 `fpga_platforms`中，VexChip 在 `VexRiscv` 中。 |
| -------------- | ------------------------------------------------------------ |
| MODEL          | 测试框架 TOP，可用于仿真或者综合，即 TestHarness             |
| VLOG_MODEL     | 仿真 TOP，一般为对应的 TestHarness                           |
| MODEL_PACKAGE  | TestHarness 所在的 Scala Package，即 `package xxx.xxx.xxx`   |
| CONFIG         | 配置名称，`XXXXConfig extends Config(... ++ ... ++ ...)`     |
| CONFIG_PACKAGE | 配置所在的 Scala Package                                     |
| TOP            | 主体 Top，一般为 `ChipTop`                                   |
| MEM            | 综合时导入的 DRM 文件名称                                    |
| CONSTRAINTS    | 综合时导入的 fdc 文件夹，`fpga/fpga-shells/pango/pgl22g/constraints/<name>` |
| SYN_TOP        | 指定 PDS 综合使用的 Top module                               |

常用/可用综合配置

| SUB_PROJECT                     | DESCRIPTION                                                  | LUT/K |
| ------------------------------- | ------------------------------------------------------------ | ----- |
| pgl22g-vexriscv                 | 单个VexRiscv在Chipyard中，含DDR                              | 9     |
| vexchip                         | VexChip系统，仅含有UART                                      | 3     |
| vexchip-debug                   | VexChip系统，含有UART、JTAG                                  | 3     |
| pgl22g-vexriscv-sim             | 单个VexRiscv在Chipyard中，使用仿真TestHarness                | 9?    |
| pgl22g-onchip-rocket-test       | RocketChip，仅含UART，DRM做主存，加载 `software/tests` 程序，不含 `mbus` | 10    |
| pgl22g-onchip-rocket-test-small | 同上但是使用 SmallCore，总体上相差不多                       | 10    |
| pgl22g-onchip-rocket-test-med   | 同上2但是使用 MedCore，使用更多 LUT                          | 12    |
| pgl22g-picorv                   | PicoRV在Chipyard中                                           | 6     |
| pgl22g-ssrv                     | SSRV在Chipyard中                                             |       |
| pgl22g-bare                     | RocketChip使用BareTestHarness：使用DDR的PLL。事实上大多TestHarness现在用的都是Bare型 |       |
| pgl22g-vexriscv2                | VexRiscv双核在Chipyard测试（x）                              |       |
| pgl22g-onchip-vexriscv-test     | VexRiscv在Chipyard，仅含UART，DRM做主存                      |       |
| pgl22g-sodor?                   | Sodor在Chipyard中                                            |       |

常用/可用仿真配置

| SUB_PROJECT                          | DESCRIPTION                                          |
| ------------------------------------ | ---------------------------------------------------- |
| vexchip                              | VexChip测试，含UART和RVFI接口                        |
| vexchip-debug                        | VexChip测试，含UART、RVFI和JTAG                      |
| default                              | Chipyard默认的RocketChip仿真                         |
| default-tiny                         | Chipyard默认的测试框架+TinyRocket配置                |
| default32-small                      | Chipyard默认测试框架+32为TinyRockey                  |
| default-cva6                         | Chipyard默认CVA6仿真                                 |
| default-cva6dmi                      | Chipyard默认CVA6仿真，打开DMI接口                    |
| pgl22g-vexriscv                      | VexRiscv in Chipyard 仿真                            |
| pgl22g-onchip                        | RocketChipOnChip(with UART only)                     |
| pgl22g-onchip-rocket-sim-{small/mid} | RocketChipOnChip装载 `software/tests`                |
| pgl22g-onchip-vexriscv-test          | VexRiscvOnChip(with UART only) 装载 `software/tests` |
| pgl22g-onchip-coremark               | RocketChipOnChip装载Coremark(旧)                     |
| pgl22g-ssrv                          | SSRV                                                 |
| pgl22g-picorv                        | PicoRV                                               |
| sodor                                | Sodor                                                |

#### VexChip 配置

| `GenVexChip`      | 仿真，No Debug        |
| ----------------- | --------------------- |
| `GenVexChipDebug` | 仿真，With Debug JTAG |
| `GenVexChipSynth` | 综合，No Debug        |

### 外设

**关于 DDR**：

1. 现在实现了两种 DDR 实现方式：`WithPGL22GTLMem`、`WithPGL22GAXIMem`
2. `WithPGL22GAXIMem` 通过 BlackBox 实现
3. `WithPGL22GTLMem` 通过 Placer 实现，不推荐使用

**关于 Flash**

1. 使用 Sifive SPIFlash

**关于 UART**

1. SpinalHDL Library 的实现
   1. 当前使用有点问题
2. Sifive 实现

**关于 DRM**

1. `mem_split.v`：对 DRM IP 的一步封装，适合 Chipyard 使用
2. `mem.v`：对 `mem_split.v` 的使用，可从生成的 `$(long_name).top.mems.v` 修改而来
3. `mem_onchip.v`：对 `mem_split.v` 的使用，OnChip 系统专用

### 仿真

通过修改 chipyard 框架内的 `sims/verilator/` 实现了对 RocketChip 和 VexChip 的仿真支持。

仿真配置在 `models-sim.mk`。

```shell
cd sims/verilator
# 按照 Chipyard 配置启动仿真，含 debug 则可生成 VCD 波形
# 编译，生成 simulate-*
make SUB_PROJECT=pgl22g-onchip-vexriscv-test
# 编译 debug 模式，生成 simulate-*-debug
make SUB_PROJECT=pgl22g-onchip-vexriscv-test debug
# 运行程序
make SUB_PROJECT=pgl22g-onchip-vexriscv-test run-binary BINARY=../../software/coremark/overlay/coremark.bare.bin
make SUB_PROJECT=pgl22g-onchip-vexriscv-test run-binary-debug BINARY=../../software/coremark/overlay/coremark.bare.bin
# 查看启动参数帮助
./simulator-chipyard.fpga.pgl22g-SimPGL22GVexRiscvConfig-debug
# 启动 VexChip 仿真
# 仿真 GenVexChip，不生成波形
make SUB_PROJECT=vexchip vex-run
# 仿真 GenVexChipDebug，并生成 FST 波形
make SUB_PROJECT=vexchip-debug vex-run-debug
```

### 综合

通过修改 chipyard 框架内的 `fpga/` 实现了对 RocketChip 和 VexChip 的仿真支持。

综合配置在 `models.mk`。

```shell
cd fpga
# Chipyard 框架
# 生成 Chipyard 框架的 Verilog
make SUB_PROJECT=pgl22g-onchip-vexriscv-test verilog
# 指定开始综合，设定综合阶段目标为 Synthesis
make SUB_PROJECT=pgl22g-onchip-vexriscv-test bitstream RUN_PROC=synth
# 指定开始综合，设定综合阶段目标为 Bitstream
make SUB_PROJECT=pgl22g-onchip-vexriscv-test bitstream RUN_PROC=bitstream
# VexChip 框架，SUB_PROJECT 的 debug 和 vexchip-bitstream[-debug] 需要一致
# 指定开始综合，设定综合阶段目标为 Create Project (Compile)
make SUB_PROJECT=vexchip vexchip-bitstream RUN_PROC=create
# 指定开始综合调试版本，设定综合阶段目标为 Implement
make SUB_PROJECT=vexchip-debug vexchip-bitstream-debug RUN_PROC=impl
```



