<%@ Page Language="C#" AutoEventWireup="true"  
         CodeFile="Default.aspx.cs" Inherits="_Default" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
          "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head runat="server">
    <title>Histogram</title>
</head>
<body>
<h1>Lag histogram</h1>
    <form id="form1" runat="server">
    <table>
    <tr>
    <td><div>
    <asp:TextBox ID="TextBox1" runat="server" Rows="12" 
                 TextMode="MultiLine" 
                 Font-Size="Large" Width="128px"></asp:TextBox>    
    </div>
    <div>        
        <asp:Button ID="Button1" runat="server" Text="Lag histogram" 
            onclick="Button1_Click" />
    </div>
    </td>
    <td>
        <asp:Image ID="Image1" runat="server" 
                   ImageUrl="~/bilde.gif"  
                   AlternateText="Histogram"
                   Width="286px" Height="271px"/>
    </td>
    </tr>
    </table>
    <div>
        <asp:Label ID="LabelMsg" runat="server" Text=":">
        </asp:Label>
    </div>
    </form>
</body>
</html>
