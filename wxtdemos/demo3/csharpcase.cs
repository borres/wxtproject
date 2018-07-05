private void buttonRemove_Click(object sender, EventArgs e)
{
	// update possible edits before we delete (and reload)
	if (this.borresDataSet.HasChanges())
		this.personTableAdapter.Update(this.borresDataSet);
	// remove selected
	int ix=dataGridView1.CurrentRow.Index;
	DataGridViewRow row=dataGridView1.Rows[ix];
	int id = Convert.ToInt32(row.Cells[0].Value);
	string navn=Convert.ToString(row.Cells[1].Value);
	String adr =Convert.ToString(row.Cells[2].Value);
	String tlf = Convert.ToString(row.Cells[3].Value);
	if (MessageBox.Show(this, "Vil du virkelig fjerne: " + navn,
							  "fjerne",MessageBoxButtons.YesNo)==
							  DialogResult.Yes)
	{
		personTableAdapter.Delete(id, navn, adr, tlf);
		this.personTableAdapter.Fill(this.borresDataSet.person);
	}
}