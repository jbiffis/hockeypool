import { useState, useEffect, useCallback } from 'react';
import { Title, FileInput, Button, Group, Alert, Card, TextInput, CopyButton, Text, Stack, SimpleGrid, Image } from '@mantine/core';

function UploadsPage() {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState(null);
  const [uploads, setUploads] = useState([]);

  const fetchUploads = useCallback(async () => {
    try {
      const res = await fetch('/api/admin/uploads', { credentials: 'include' });
      if (res.ok) setUploads(await res.json());
    } catch { /* ignore */ }
  }, []);

  useEffect(() => { fetchUploads(); }, [fetchUploads]);

  async function handleUpload() {
    if (!file) return;
    setUploading(true);
    setError(null);
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
      setFile(null);
      fetchUploads();
    } catch (e) {
      setError(e.message);
    } finally {
      setUploading(false);
    }
  }

  return (
    <div>
      <Title order={1} mb="md">Uploads</Title>

      <Card withBorder maw={500} mb="xl">
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
        </Stack>
      </Card>

      {uploads.length > 0 && (
        <>
          <Title order={2} mb="sm">Uploaded images</Title>
          <SimpleGrid cols={{ base: 1, sm: 2, md: 3 }} spacing="sm">
            {uploads.map((u) => (
              <Card key={u.filename} withBorder padding="sm">
                <Image
                  src={u.url}
                  alt={u.filename}
                  height={120}
                  fit="contain"
                  mb="xs"
                  bg="gray.1"
                  radius="sm"
                />
                <Group gap="xs">
                  <TextInput value={u.url} readOnly size="xs" style={{ flex: 1 }} />
                  <CopyButton value={u.url}>
                    {({ copied, copy }) => (
                      <Button variant="default" size="xs" onClick={copy}>
                        {copied ? 'Copied' : 'Copy'}
                      </Button>
                    )}
                  </CopyButton>
                </Group>
              </Card>
            ))}
          </SimpleGrid>
        </>
      )}

      {uploads.length === 0 && (
        <Text c="dimmed">No images uploaded yet.</Text>
      )}
    </div>
  );
}

export default UploadsPage;
