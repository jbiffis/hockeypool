import { useState } from 'react';
import { Title, FileInput, Button, Group, Alert, Card, TextInput, CopyButton, Text, Stack } from '@mantine/core';

function UploadsPage() {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadedUrl, setUploadedUrl] = useState(null);
  const [error, setError] = useState(null);

  async function handleUpload() {
    if (!file) return;
    setUploading(true);
    setError(null);
    setUploadedUrl(null);
    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await fetch('/api/admin/upload', {
        method: 'POST',
        body: formData,
        credentials: 'include',
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Upload failed');
      setUploadedUrl(data.url);
      setFile(null);
    } catch (e) {
      setError(e.message);
    } finally {
      setUploading(false);
    }
  }

  return (
    <div>
      <Title order={1} mb="md">Upload File</Title>

      <Card withBorder maw={500}>
        <Stack>
          <FileInput
            label="Select image"
            placeholder="Click to select..."
            accept="image/*"
            value={file}
            onChange={setFile}
          />
          <Button onClick={handleUpload} loading={uploading} disabled={!file}>
            Upload
          </Button>
          {error && <Alert color="red">{error}</Alert>}
          {uploadedUrl && (
            <Stack gap="xs">
              <Text size="sm" fw={500}>Public URL:</Text>
              <Group gap="xs">
                <TextInput value={uploadedUrl} readOnly style={{ flex: 1 }} />
                <CopyButton value={uploadedUrl}>
                  {({ copied, copy }) => (
                    <Button variant="default" size="sm" onClick={copy}>
                      {copied ? 'Copied' : 'Copy'}
                    </Button>
                  )}
                </CopyButton>
              </Group>
            </Stack>
          )}
        </Stack>
      </Card>
    </div>
  );
}

export default UploadsPage;
