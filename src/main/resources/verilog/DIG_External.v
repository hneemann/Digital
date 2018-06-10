<?=elem.Code;

  moduleName=elem.Label;

  if (elem.applicationType!="IVERILOG")
    panic("err_canOnlyExportExternalVerilog");

?>