#!/usr/bin/env bash
# Runs ON Server A (as root) when the GitHub workflow sends it through SSM Run Command.
# Usage: install.sh <bucket> <key-of-the-war-file>
#
# Same steps as the old SSH deploy: back up the current WAR, keep the 5 newest
# backups, install the new WAR so Tomcat redeploys it, then check the app answers.
set -euo pipefail

BUCKET="$1"
WAR_KEY="$2"

TOMCAT_HOME="/usr/local/apache-tomcat-9.0.102"
WEBAPPS="$TOMCAT_HOME/webapps"
APP_NAME="the-hobby-lobby"   # name used for the download and the backups
DEPLOY_AS="ROOT"             # ROOT.war is served at "/", so the subdomain opens the app directly
HEALTH_URL="http://localhost:8080/"   # Tomcat's own port, checked from the server itself

# Run Command runs as root, where ~ means /root. Pin the paths to ec2-user's home
# so backups stay where the old SSH deploy kept them.
DEPLOY_USER="ec2-user"
BACKUP_DIR="/home/$DEPLOY_USER/war-backups"
STAMP=$(date +%Y%m%d-%H%M%S)
TMP_WAR="/tmp/$APP_NAME.war"

echo "Downloading s3://$BUCKET/$WAR_KEY"
aws s3 cp "s3://$BUCKET/$WAR_KEY" "$TMP_WAR" --only-show-errors

# Keep the previous WAR so you can roll back.
mkdir -p "$BACKUP_DIR"
chown "$DEPLOY_USER:$DEPLOY_USER" "$BACKUP_DIR"
if [ -f "$WEBAPPS/$DEPLOY_AS.war" ]; then
  BACKUP="$BACKUP_DIR/$APP_NAME-$STAMP.war"
  cp "$WEBAPPS/$DEPLOY_AS.war" "$BACKUP"
  chown "$DEPLOY_USER:$DEPLOY_USER" "$BACKUP"
  echo "Backed up old WAR to $BACKUP"
fi

# Keep only the 5 newest backups.
ls -1t "$BACKUP_DIR"/$APP_NAME-*.war 2>/dev/null | tail -n +6 | xargs -r rm -f || true

# Install the new WAR. Tomcat notices the change and redeploys.
cp "$TMP_WAR" "$WEBAPPS/$DEPLOY_AS.war"
chown tomcat:tomcat "$WEBAPPS/$DEPLOY_AS.war"
rm -f "$TMP_WAR"
echo "Installed new $DEPLOY_AS.war"

# Health check: give Tomcat time to swap apps, then wait up to 2 minutes for an answer.
sleep 20
for i in $(seq 1 24); do
  CODE=$(curl -s -o /dev/null -w '%{http_code}' "$HEALTH_URL" || true)
  if [[ "$CODE" =~ ^[23] ]]; then
    echo "Health check passed (HTTP $CODE)"
    exit 0
  fi
  echo "Waiting for the app... (HTTP $CODE)"
  sleep 5
done
echo "Health check FAILED: $HEALTH_URL never answered. Roll back with a WAR from $BACKUP_DIR"
exit 1
