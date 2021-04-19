<?=loadFile(elem.CodeFile);

  moduleName=elem.Label;

  if (elem.applicationType!="IVERILOG")
    panic("err_canOnlyExportExternalVerilog");

?>