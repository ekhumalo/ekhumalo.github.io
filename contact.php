
<?php
if($_POST["message"]) {
    mail("ekhumalo@usapglobal.org", "Form to email message", $_POST["message"], "From: an@email.address");
}
?>

<form method="post" action="contact.php">
    <textarea name="message"></textarea>
    <input type="submit">
</form>