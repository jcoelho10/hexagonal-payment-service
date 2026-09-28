oci db system launch \
  --compartment-id <OCID_DO_SEU_COMPARTMENT> \
  --availability-domain "U44C:US-ASHBURN-AD-1" \
  --database-edition "STANDARD_EDITION" \
  --db-name "payment_db" \
  --shape "VM.Standard.E4.Flex" \
  --node-count 1 \
  --ssh-public-keys-file ~/.ssh/id_rsa.pub \
  --admin-password "YourStrongPassword123!"