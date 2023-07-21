# VM Import Tool for VMWare ESXi Server

A tiny tool in Network Management undergraduate course major project which make it easier to clone VMs into ESXi host.<br>
Required: OVFTool and PowerCLI on Windows PowerShell.<br><br>

Screenshots:
- Initially, enter host credentials and check host connection.<br><br>
  ![image](https://github.com/dtduy2k1/vm-import-tool/assets/64970914/2db8da79-45c2-45c8-ae5d-7b31ec9abe09)
- After successfully connection, enter VN information and select operating system. Currently supported: Windows 7 Home and Ubuntu 22.04 LTS.<br><br>
![image](https://github.com/dtduy2k1/vm-import-tool/assets/64970914/f44a5258-77af-48ea-be94-da6a2b382da7)
- Tool processing (basically run OVFTool bash scripts to import a OVA into host and run PowerCLI scripts to modify VM information).<br><br>
![image](https://github.com/dtduy2k1/vm-import-tool/assets/64970914/39e3b344-95f4-4245-ad28-bb4112975e01)
![image](https://github.com/dtduy2k1/vm-import-tool/assets/64970914/b90ed04f-a94a-4a83-8206-cbb8bb63302e)
- Final result, VM created on server showed on EXSi Host Client.<br><br>
![image](https://github.com/dtduy2k1/vm-import-tool/assets/64970914/e79efe0b-1133-4df0-a850-ade2edf81c18)<br><br><br>
2 days spent right before presentaion. These scripts works fine on my machine, yet I'm not sure on others.<br><br>


